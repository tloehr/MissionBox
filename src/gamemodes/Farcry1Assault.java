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







//    private final RelaySiren relaisSirens, relaisLEDs;


    private Farcry1AssaultThread farcryAssaultThread;


    private int prev_countdown_index;



    public Farcry1Assault() throws IOException {



        logger.setLevel(MissionBox.getLogLevel());

        // hier gehts weiter






        MessageListener textListener = messageEvent -> logger.debug(messageEvent.getMessage().toString());

        MessageListener gameTimeListener = messageEvent -> {
            if (messageEvent.getMode() == Farcry1AssaultThread.GAME_PRE_GAME) {
                MissionBox.setTimerMessage("--");
                return;
            }
            if (messageEvent.getMode() == Farcry1AssaultThread.GAME_OVER) {
                MissionBox.setTimerMessage(gameWon ? "Flag taken" : "Flag defended");
                return;
            }
            logger.debug("GameTime: " + messageEvent.getMessage());
            MissionBox.setTimerMessage(messageEvent.getMessage().toString());
        };

        MessageListener percentageListener = messageEvent -> {
            logger.debug(messageEvent.getPercentage());
            if (MissionBox.isSIREN()) MissionBox.getRelaisSirens().setValue(messageEvent.getPercentage());
            MissionBox.setProgress(messageEvent.getPercentage().intValue());

            if (messageEvent.getMode() == Farcry1AssaultThread.GAME_FLAG_HOT) {
                int countdown_index = messageEvent.getPercentage().intValue() / 10;
                if (prev_countdown_index != countdown_index) {
                    prev_countdown_index = countdown_index;
                    MissionBox.countdown(countdown_index);
                }
            }
        };

        MessageListener gameModeListener = messageEvent -> {
            logger.debug("gameMode changed: " + Farcry1AssaultThread.GAME_MODES[messageEvent.getMode()]);
            MissionBox.setMessage(Farcry1AssaultThread.GAME_MODES[messageEvent.getMode()]);

            if (messageEvent.getMode() == Farcry1AssaultThread.GAME_FLAG_HOT) {
                MissionBox.stop("shutdown");
                MissionBox.play("siren", true);
                MissionBox.getLedRed().blink(100, PinState.HIGH);
                MissionBox.getLedGreen().blink(100, PinState.LOW);
            } else if (messageEvent.getMode() == Farcry1AssaultThread.GAME_FLAG_COLD) {
                MissionBox.stop("siren");

                if (prev_countdown_index > -1) MissionBox.play("shutdown"); // plays only when the flag has been touched during this round.

                MissionBox.getLedRed().blink(1000, PinState.HIGH);
                MissionBox.getLedGreen().blink(1000, PinState.LOW);
                MissionBox.getLedBarGreen().blink(0);
                MissionBox.getLedBarYellow().blink(0);
                MissionBox.getLedBarRed().blink(0);
                MissionBox.setRelaySirenPercentage(BigDecimal.ZERO);
            } else if (messageEvent.getMode() == Farcry1AssaultThread.GAME_ROCKET_LAUNCHED) {
                MissionBox.stop("siren");
                MissionBox.setRelaySirenPercentage(BigDecimal.ZERO);
                MissionBox.play("rocket");
                MissionBox.getLedRed().blink(50, PinState.HIGH);
                MissionBox.getLedGreen().blink(50, PinState.LOW);
                MissionBox.getLedBarGreen().blink(50);
                MissionBox.getLedBarYellow().blink(50);
                MissionBox.getLedBarRed().blink(50);
                gameWon = true;
            } else if (messageEvent.getMode() == Farcry1AssaultThread.GAME_PRE_GAME) {
                gameWon = false;
                prev_countdown_index = -1;



                MissionBox.stopAllSongs();

                MissionBox.enableSettings(true);

                MissionBox.getLedRed().blink(500, PinState.HIGH);
                MissionBox.getLedGreen().blink(500, PinState.LOW);
                MissionBox.getLedBarGreen().blink(1000);
                MissionBox.getLedBarYellow().blink(1000);
                MissionBox.getLedBarRed().blink(1000);

                MissionBox.stop("siren");
                MissionBox.stop("rocket");
                MissionBox.play("welcome");

            } else if (messageEvent.getMode() == Farcry1AssaultThread.GAME_OVER) {
                MissionBox.stop("siren");
                MissionBox.stop("rocket");
                MissionBox.getLedGreen().blink(0);
                if (gameWon) {
                    MissionBox.stop("victory");
                    MissionBox.playWinner();

                } else {
                    MissionBox.stop("defeat");
                    MissionBox.playLooser();

                }
//                fadeout(playWinningSon);
            } else if (messageEvent.getMode() == Farcry1AssaultThread.GAME_OUTCOME_FLAG_TAKEN) {
                MissionBox.getLedRed().blink(250, PinState.HIGH);
                MissionBox.getLedGreen().blink(250, PinState.HIGH);
                MissionBox.getLedBarGreen().blink(500);
                MissionBox.getLedBarYellow().blink(500);
                MissionBox.getLedBarRed().blink(500);
                MissionBox.stop("siren");
                MissionBox.stop("rocket");

            } else if (messageEvent.getMode() == Farcry1AssaultThread.GAME_FLAG_ACTIVE) {
                MissionBox.enableSettings(false);
                MissionBox.stop("minions");



            }
        };

        farcryAssaultThread = new Farcry1AssaultThread(textListener, gameTimeListener, percentageListener, gameModeListener);

        MissionBox.getBtnRed().addListener((GpioPinListenerDigital) event -> {
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



//        farcryAssaultThread.quitGame();
//
//        playSiren.unload();
//        playRocket.unload();
//        playWinningSong.unload();
//        playLoserSong.unload();
//        playVictory.unload();
//        playDefeat.unload();
//        TinyMissionBox.isSOUND().shutdown();

        System.exit(0);

    }

}
