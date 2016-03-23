package gamemodes;

import com.pi4j.gpio.extension.mcp.MCP23017GpioProvider;
import com.pi4j.gpio.extension.mcp.MCP23017Pin;
import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.io.i2c.I2CBus;
import interfaces.MessageListener;
import interfaces.MyAbstractButton;
import interfaces.Relay;
import interfaces.RelaySiren;
import kuusisto.tinysound.Music;
import kuusisto.tinysound.Sound;
import kuusisto.tinysound.TinySound;
import main.FrmTest;
import main.MissionBox;
import misc.Tools;
import org.apache.log4j.Logger;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

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





//    private final RelaySiren relaisSirens, relaisLEDs;


    private Farcry1AssaultThread farcryAssaultThread;

    private Music playSiren, playWinningSong, playLoserSong;
    private Sound playWelcome, playRocket, playMinions, playGameOver, playVictory, playDefeat;

    private Sound[] countdown = new Sound[11];
    private int prev_countdown_index;



    public Farcry1Assault(FrmTest frmTest) throws IOException {



        logger.setLevel(MissionBox.logLevel);

        // hier gehts weiter

        final GpioPinDigitalInput ioRed = MissionBox.getGPIO() == null ? null : MissionBox.getGPIO().provisionDigitalInputPin(RaspiPin.GPIO_00, "RedTrigger", PinPullResistance.PULL_DOWN);
        MyAbstractButton btnRed = new MyAbstractButton(ioRed, frmTest.getBtnRed());

        final GpioPinDigitalInput ioGreen = MissionBox.getGPIO() == null ? null : MissionBox.getGPIO().provisionDigitalInputPin(RaspiPin.GPIO_02, "GreenTrigger", PinPullResistance.PULL_DOWN);
        MyAbstractButton btnGreen = new MyAbstractButton(ioGreen, frmTest.getBtnGreen());

        final GpioPinDigitalInput ioGameStartStop = MissionBox.getGPIO() == null ? null : MissionBox.getGPIO().provisionDigitalInputPin(RaspiPin.GPIO_03, "GameStartStop", PinPullResistance.PULL_DOWN);
        MyAbstractButton btnGameStartStop = new MyAbstractButton(ioGameStartStop, frmTest.getBtn1());

        final GpioPinDigitalInput ioMisc = MissionBox.getGPIO() == null ? null : MissionBox.getGPIO().provisionDigitalInputPin(RaspiPin.GPIO_21, "MISC", PinPullResistance.PULL_DOWN);
        MyAbstractButton btnMisc = new MyAbstractButton(ioMisc, frmTest.getBtn2());




        TinySound.init();

        playSiren = TinySound.loadMusic(new File(Tools.getSoundPath() + File.separator + Tools.SND_SIREN));
        playWelcome = TinySound.loadSound(new File(Tools.getSoundPath() + File.separator + Tools.SND_WELCOME));
        playRocket = TinySound.loadSound(new File(Tools.getSoundPath() + File.separator + Tools.SND_FLARE));
        playGameOver = TinySound.loadSound(new File(Tools.getSoundPath() + File.separator + Tools.SND_GAME_OVER));
        playMinions = TinySound.loadSound(new File(Tools.getSoundPath() + File.separator + Tools.SND_MINIONS_SPAWNED));
        playVictory = TinySound.loadSound(new File(Tools.getSoundPath() + File.separator + Tools.SND_VICTORY));
        playDefeat = TinySound.loadSound(new File(Tools.getSoundPath() + File.separator + Tools.SND_DEFEAT));

        for (int i = 0; i <= 10; i++) {
            countdown[i] = TinySound.loadSound(new File(Tools.getSoundPath() + File.separator + Tools.COUNTDOWN[i]));
        }

        MessageListener textListener = messageEvent -> logger.debug(messageEvent.getMessage().toString());

        MessageListener gameTimeListener = messageEvent -> {
            if (messageEvent.getMode() == Farcry1AssaultThread.GAME_PRE_GAME) {
                frmTest.setTimer("--");
                return;
            }
            logger.debug("GameTime: " + messageEvent.getMessage());
            frmTest.setTimer(messageEvent.getMessage().toString());
        };

        MessageListener percentageListener = messageEvent -> {
            logger.debug(messageEvent.getPercentage());
            if (siren) MissionBox.getRelaisSirens().setValue(messageEvent.getPercentage());
            frmTest.setProgress(messageEvent.getPercentage().intValue());

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
            frmTest.setMessage(Farcry1AssaultThread.GAME_MODES[messageEvent.getMode()]);

            if (messageEvent.getMode() == Farcry1AssaultThread.GAME_FLAG_HOT) {
                if (sound) playSiren.play(true);
                MissionBox.getLedRed().blink(100, PinState.HIGH);
                MissionBox.getLedGreen().blink(100, PinState.LOW);
            } else if (messageEvent.getMode() == Farcry1AssaultThread.GAME_FLAG_COLD) {
                if (sound) playSiren.stop();
                MissionBox.getLedRed().blink(1000, PinState.HIGH);
                MissionBox.getLedGreen().blink(1000, PinState.LOW);
                MissionBox.getLedBarGreen().blink(0);
                MissionBox.getLedBarYellow().blink(0);
                MissionBox.getLedBarRed().blink(0);
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
                    playLoserSong.unload();
                    playWinningSong.unload();
                }

                playLoserSong = TinySound.loadMusic(new File(Tools.getSoundPath() + File.separator + Tools.getLosingSong()));
                playWinningSong = TinySound.loadMusic(new File(Tools.getSoundPath() + File.separator + Tools.getWinningSong()));

                frmTest.enableSettings(true);

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
//                fadeout(playWinningSon);
            } else if (messageEvent.getMode() == Farcry1AssaultThread.GAME_OUTCOME_FLAG_TAKEN) {
                MissionBox.getLedRed().blink(250, PinState.HIGH);
                MissionBox.getLedGreen().blink(250, PinState.HIGH);
                MissionBox.getLedBarGreen().blink(500);
                MissionBox.getLedBarYellow().blink(500);
                MissionBox.getLedBarRed().blink(500);
                if (sound) playSiren.stop();
                if (sound) playRocket.stop();

            } else if (messageEvent.getMode() == Farcry1AssaultThread.GAME_FLAG_ACTIVE) {
                frmTest.enableSettings(false);
                if (sound) playMinions.play();


            }
        };

        farcryAssaultThread = new Farcry1AssaultThread(textListener, gameTimeListener, percentageListener, gameModeListener);

        btnRed.addListener((GpioPinListenerDigital) event -> {
            if (event.getState() == PinState.HIGH) {
                logger.debug("RedButton pressed");
                farcryAssaultThread.setFlag(true);
            }
        });

        btnRed.addListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logger.debug("RedButton pressed");
                farcryAssaultThread.setFlag(true);
            }
        });

        btnGreen.addListener((GpioPinListenerDigital) event -> {
            if (event.getState() == PinState.HIGH) {
                // If both buttons are pressed, the red one wins.
                if (btnRed.isHigh()) return;
                logger.debug("GreenButton pressed");
                farcryAssaultThread.setFlag(false);
            }
        });

        btnGreen.addListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logger.debug("GreenButton pressed");
                farcryAssaultThread.setFlag(false);
            }
        });

        btnGameStartStop.addListener((GpioPinListenerDigital) event -> {
            if (!frmTest.isGameStartable()) return;
            if (event.getState() == PinState.HIGH) {
                logger.debug("btnGameStartStop");
                if (farcryAssaultThread.getGameState() == Farcry1AssaultThread.GAME_PRE_GAME) {
                    farcryAssaultThread.startGame();
                } else {
                    farcryAssaultThread.restartGame();
                }
            }
        });

        btnGameStartStop.addListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!frmTest.isGameStartable()) return;
                logger.debug("btnGameStartStop");
                if (farcryAssaultThread.getGameState() == Farcry1AssaultThread.GAME_PRE_GAME) {
                    farcryAssaultThread.startGame();
                } else {
                    farcryAssaultThread.restartGame();
                }
            }
        });

        btnMisc.addListener((GpioPinListenerDigital) event -> {


//            fadeout(playWinningSon);


            quitGame();
        });

        btnMisc.addListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                quitGame();
            }
        });

        farcryAssaultThread.run();

    }

//    void fadeout(Music music) {
//        SwingWorker worker = new SwingWorker() {
//            double volume;
//
//            @Override
//            protected Object doInBackground() throws Exception {
//                volume = music.getVolume();
//
//                for (double vol = volume; vol >= 0d; vol = vol - 0.01d) {
//                    logger.debug(vol);
//                    music.setVolume(vol);
//                    Thread.sleep(50);
//                }
//
//                return null;
//            }
//
//            @Override
//            protected void done() {
//                super.done();
//                music.stop();
//                music.setVolume(volume);
//            }
//        };
//        worker.run();
//    }

    @Override
    public void quitGame() {

        farcryAssaultThread.quitGame();

//        Lcd.lcdClear(lcdHandle);
        farcryAssaultThread.quitGame();
//        relayRocket.setOff();
//        relayStrobe.setOff();
//        ledBar.setOff();
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
