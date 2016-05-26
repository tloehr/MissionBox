package gamemodes;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import interfaces.MessageListener;
import kuusisto.tinysound.Music;
import kuusisto.tinysound.Sound;
import kuusisto.tinysound.TinySound;
import main.MissionBox;
import misc.Tools;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;

/**
 * Created by tloehr on 31.05.15.
 */
public class Farcry1Assault implements GameModes {
    private final Logger logger = Logger.getLogger(getClass());

    private boolean gameWon = false;

    // the game is organized in cycles. In a cycle the game state is checked and it is decided if the game was won or not.
    private final int MILLISPERCYCLE = 50;

    private boolean sound = Boolean.parseBoolean(MissionBox.getConfig().getProperty(MissionBox.FCY_SOUND));
    private boolean siren = Boolean.parseBoolean(MissionBox.getConfig().getProperty(MissionBox.FCY_SIREN));

    private Farcry1AssaultThread farcryAssaultThread;

    private Music playSiren, playWinningSong, playLoserSong;
    private Sound playWelcome, playRocket, playMinions, playGameOver, playVictory, playDefeat, playShutdown;

    private Sound[] countdown = new Sound[11];
    private int prev_countdown_index;


    public Farcry1Assault() throws IOException {
        logger.setLevel(MissionBox.logLevel);

        TinySound.init();

        playSiren = TinySound.loadMusic(new File(Tools.getSoundPath() + File.separator + Tools.SND_SIREN));
        playWelcome = TinySound.loadSound(new File(Tools.getSoundPath() + File.separator + Tools.SND_WELCOME));
        playRocket = TinySound.loadSound(new File(Tools.getSoundPath() + File.separator + Tools.SND_FLARE));
        playGameOver = TinySound.loadSound(new File(Tools.getSoundPath() + File.separator + Tools.SND_GAME_OVER));
        playMinions = TinySound.loadSound(new File(Tools.getSoundPath() + File.separator + Tools.SND_MINIONS_SPAWNED));
        playVictory = TinySound.loadSound(new File(Tools.getSoundPath() + File.separator + Tools.SND_VICTORY));
        playDefeat = TinySound.loadSound(new File(Tools.getSoundPath() + File.separator + Tools.SND_DEFEAT));
        playShutdown = TinySound.loadSound(new File(Tools.getSoundPath() + File.separator + Tools.SND_SHUTDOWN));
        playLoserSong = TinySound.loadMusic(new File(Tools.getSoundPath() + File.separator + Tools.getLosingSong()));
        playWinningSong = TinySound.loadMusic(new File(Tools.getSoundPath() + File.separator + Tools.getWinningSong()));

        for (int i = 0; i <= 10; i++) {
            countdown[i] = TinySound.loadSound(new File(Tools.getSoundPath() + File.separator + Tools.COUNTDOWN[i]));
        }

        MessageListener textListener = messageEvent -> logger.debug(messageEvent.getMessage().toString());

        MessageListener gameTimeListener = messageEvent -> {
            if (messageEvent.getMode() == Farcry1AssaultThread.GAME_PRE_GAME) {
//                frmTest.setTimer("--");
                return;
            }
            if (messageEvent.getMode() == Farcry1AssaultThread.GAME_OVER) {
//                frmTest.setTimer(gameWon ? "Flag taken" : "Flag defended");
                return;
            }
            logger.info("GameTime: " + messageEvent.getMessage());
        };

        MessageListener percentageListener = messageEvent -> {
            logger.debug(messageEvent.getPercentage());
            if (siren) MissionBox.getRelaisSirens().setValue(messageEvent.getPercentage());

            if (messageEvent.getMode() == Farcry1AssaultThread.GAME_FLAG_HOT) {
                int countdown_index = messageEvent.getPercentage().intValue() / 10;
                if (prev_countdown_index != countdown_index) {
                    prev_countdown_index = countdown_index;
                    if (sound) countdown[countdown_index].play();
                }
            }
        };

        MessageListener gameModeListener = messageEvent -> {
            logger.debug("gameMode changed: " + Farcry1AssaultThread.GAME_MODES[messageEvent.getMode()]);

            if (messageEvent.getMode() == Farcry1AssaultThread.GAME_FLAG_HOT) {
                if (sound) playShutdown.stop();
                if (sound) playSiren.play(true);
                MissionBox.getLedRed().blink(100, PinState.HIGH);
                MissionBox.getLedGreen().blink(100, PinState.LOW);
            } else if (messageEvent.getMode() == Farcry1AssaultThread.GAME_FLAG_COLD) {
                if (sound) playSiren.stop();
                if (sound && prev_countdown_index > -1)
                    playShutdown.play(); // plays only when the flag has been touched during this round.

                MissionBox.getLedRed().blink(1000, PinState.HIGH);
                MissionBox.getLedGreen().blink(1000, PinState.LOW);
                MissionBox.getLedBarGreen().setOn(false);
                MissionBox.getLedBarYellow().setOn(false);
                MissionBox.getLedBarRed().setOn(false);
                if (siren) MissionBox.getRelaisSirens().setValue(BigDecimal.ZERO);
            } else if (messageEvent.getMode() == Farcry1AssaultThread.GAME_ROCKET_LAUNCHED) {
                if (sound) playSiren.stop();
                if (siren) MissionBox.getRelaisSirens().setValue(BigDecimal.ZERO);
                if (sound) playRocket.play();
                MissionBox.getLedRed().blink(50, PinState.HIGH);
                MissionBox.getLedGreen().blink(50, PinState.LOW);
                MissionBox.getLedBarGreen().blink(50);
                MissionBox.getLedBarYellow().blink(50);
                MissionBox.getLedBarRed().blink(50);
                gameWon = true;
            } else if (messageEvent.getMode() == Farcry1AssaultThread.GAME_PRE_GAME) {
                gameWon = false;
                prev_countdown_index = -1;

                if (sound && playWinningSong != null) { // checking for 1 null is enough.
                    playWinningSong.stop();
                    playLoserSong.stop();
                }
                MissionBox.getLedRed().blink(500, PinState.HIGH);
                MissionBox.getLedGreen().blink(500, PinState.LOW);
                MissionBox.getLedBarGreen().blink(1000);
                MissionBox.getLedBarYellow().blink(1000);
                MissionBox.getLedBarRed().blink(1000);

                if (sound) playSiren.stop();
                if (sound) playRocket.stop();

                if (sound) playWelcome.play();

            } else if (messageEvent.getMode() == Farcry1AssaultThread.GAME_OVER) {
                if (sound) playSiren.stop();
                if (sound) playRocket.stop();
                MissionBox.getLedGreen().blink(0);
                if (gameWon) {
                    if (sound) playVictory.play();
                    if (sound) playWinningSong.play(false);
                } else {
                    if (sound) playDefeat.play();
                    if (sound) playLoserSong.play(false);
                }

            } else if (messageEvent.getMode() == Farcry1AssaultThread.GAME_OUTCOME_FLAG_TAKEN) {
                MissionBox.getLedRed().blink(250, PinState.HIGH);
                MissionBox.getLedGreen().blink(250, PinState.HIGH);
                MissionBox.getLedBarGreen().blink(500);
                MissionBox.getLedBarYellow().blink(500);
                MissionBox.getLedBarRed().blink(500);
                if (sound) playSiren.stop();
                if (sound) playRocket.stop();

            } else if (messageEvent.getMode() == Farcry1AssaultThread.GAME_FLAG_ACTIVE) {
                if (sound) playMinions.play();
            }
        };

        MissionBox.getIoRed().addListener((GpioPinListenerDigital) event -> {
            logger.info("RedButton pressed");
            if (event.getState() == PinState.HIGH) {
                farcryAssaultThread.setFlag(true);
            }
        });

        MissionBox.getIoGreen().addListener((GpioPinListenerDigital) gpioPinDigitalStateChangeEvent -> {
            logger.info("GreenButton pressed");
            if (gpioPinDigitalStateChangeEvent.getState() == PinState.HIGH) {
                // If both buttons are pressed, the red one wins.
                if (MissionBox.getIoRed().isHigh()) return;
                farcryAssaultThread.setFlag(false);
            }
        });

        MissionBox.getIoGameStartStop().addListener((GpioPinListenerDigital) event -> {
            logger.info("btnGameStartStop");
            if (event.getState() == PinState.HIGH) {
                if (farcryAssaultThread.getGameState() == Farcry1AssaultThread.GAME_PRE_GAME) {
                    farcryAssaultThread.startGame();
                } else {
                    farcryAssaultThread.restartGame();
                }
            }
        });

        MissionBox.getIoMisc().addListener((GpioPinListenerDigital) event -> {
            logger.info("btnQuitGamePressed");
            quitGame();
        });

        farcryAssaultThread = new Farcry1AssaultThread(textListener, gameTimeListener, percentageListener, gameModeListener);
        farcryAssaultThread.run();
    }


    @Override
    public void quitGame() {
        farcryAssaultThread.quitGame();
        farcryAssaultThread.quitGame();
        playSiren.unload();
        playRocket.unload();
        playWinningSong.unload();
        playLoserSong.unload();
        playVictory.unload();
        playDefeat.unload();
        TinySound.shutdown();

        System.exit(0);
    }

}
