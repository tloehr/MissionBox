package gamemodes;

import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import interfaces.MessageListener;
import main.MissionBox;
import org.apache.log4j.Logger;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.math.BigDecimal;

/**
 * Created by tloehr on 31.05.15.
 */
public class Farcry1Assault implements GameModes {
    private final Logger logger = Logger.getLogger(getClass());
    private boolean gameWon = false;
    private Farcry1AssaultThread farcryAssaultThread;
    private int prev_countdown_index, gameTimeNotificationCouner = 0;


    public Farcry1Assault() throws IOException {



        logger.setLevel(MissionBox.getLogLevel());

        // hier gehts weiter






        MessageListener textListener = messageEvent -> logger.debug(messageEvent.getMessage().toString());

        MessageListener gameTimeListener = messageEvent -> {
            gameTimeNotificationCouner++;
            if (gameTimeNotificationCouner >= 50){
                gameTimeNotificationCouner = 0;
            } else {
                return;
            }

            if (messageEvent.getMode() == Farcry1AssaultThread.GAME_PRE_GAME) {
                MissionBox.setTimerMessage("--");
                return;
            }
            if (messageEvent.getMode() == Farcry1AssaultThread.GAME_OVER) {
                MissionBox.setTimerMessage(gameWon ? "Flag taken" : "Flag defended");
                return;
            }

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
                MissionBox.blink("ledRed",100, PinState.HIGH);
                MissionBox.blink("ledGreen",100, PinState.LOW);
            } else if (messageEvent.getMode() == Farcry1AssaultThread.GAME_FLAG_COLD) {
                MissionBox.stop("siren");

                if (prev_countdown_index > -1) MissionBox.play("shutdown"); // plays only when the flag has been touched during this round.

                MissionBox.blink("ledRed",1000, PinState.HIGH);
                MissionBox.blink("ledGreen",1000, PinState.LOW);
                MissionBox.blink("ledBarGreen",0);
                MissionBox.blink("ledBarYellow",0);
                MissionBox.blink("ledBarRed",0);
                MissionBox.setRelaySirenPercentage(BigDecimal.ZERO);
            } else if (messageEvent.getMode() == Farcry1AssaultThread.GAME_ROCKET_LAUNCHED) {
                MissionBox.stop("siren");
                MissionBox.setRelaySirenPercentage(BigDecimal.ZERO);
                MissionBox.play("rocket");

                MissionBox.blink("ledRed",50, PinState.HIGH);
                MissionBox.blink("ledGreen",50, PinState.LOW);
                MissionBox.blink("ledBarGreen",50);
                MissionBox.blink("ledBarYellow",50);
                MissionBox.blink("ledBarRed",50);
                gameWon = true;
            } else if (messageEvent.getMode() == Farcry1AssaultThread.GAME_PRE_GAME) {
                gameWon = false;
                prev_countdown_index = -1;

                MissionBox.stopAllSongs();
                MissionBox.enableSettings(true);

                MissionBox.blink("ledRed",500, PinState.HIGH);
                MissionBox.blink("ledGreen",500, PinState.LOW);
                MissionBox.blink("ledBarGreen",1000);
                MissionBox.blink("ledBarYellow",1000);
                MissionBox.blink("ledBarRed",1000);

                MissionBox.stop("siren");
                MissionBox.stop("rocket");
                MissionBox.play("welcome");

            } else if (messageEvent.getMode() == Farcry1AssaultThread.GAME_OVER) {
                MissionBox.stop("siren");
                MissionBox.stop("rocket");
                MissionBox.blink("ledGreen",0);

                if (gameWon) {
                    MissionBox.stop("victory");
                    MissionBox.playWinner();

                } else {
                    MissionBox.stop("defeat");
                    MissionBox.playLooser();

                }
            } else if (messageEvent.getMode() == Farcry1AssaultThread.GAME_OUTCOME_FLAG_TAKEN) {

                MissionBox.blink("ledRed",250, PinState.HIGH);
                MissionBox.blink("ledGreen",250, PinState.LOW);
                MissionBox.blink("ledBarGreen",500);
                MissionBox.blink("ledBarYellow",500);
                MissionBox.blink("ledBarRed",500);

                MissionBox.stop("siren");
                MissionBox.stop("rocket");

            } else if (messageEvent.getMode() == Farcry1AssaultThread.GAME_FLAG_ACTIVE) {
                MissionBox.enableSettings(false);
                MissionBox.play("minions");

            }
        };

        farcryAssaultThread = new Farcry1AssaultThread(textListener, gameTimeListener, percentageListener, gameModeListener);

        MissionBox.getBtnRed().addListener((GpioPinListenerDigital) event -> {
            if (event.getState() == PinState.HIGH) {
                logger.debug("RedButton pressed");
                farcryAssaultThread.setFlag(true);
            }
        });

        MissionBox.getBtnRed().addListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logger.debug("RedButton pressed");
                farcryAssaultThread.setFlag(true);
            }
        });

        MissionBox.getBtnGreen().addListener((GpioPinListenerDigital) event -> {
            if (event.getState() == PinState.HIGH) {
                // If both buttons are pressed, the red one wins.
                if ( MissionBox.getBtnRed().isHigh()) return;
                logger.debug("GreenButton pressed");
                farcryAssaultThread.setFlag(false);
            }
        });

        MissionBox.getBtnGreen().addListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logger.debug("GreenButton pressed");
                farcryAssaultThread.setFlag(false);
            }
        });

        MissionBox.getBtnGameStartStop().addListener((GpioPinListenerDigital) event -> {
            if (!MissionBox.isGameStartable()) return;
            if (event.getState() == PinState.HIGH) {
                logger.debug("btnGameStartStop");
                if (farcryAssaultThread.getGameState() == Farcry1AssaultThread.GAME_PRE_GAME) {
                    farcryAssaultThread.startGame();
                } else {
                    farcryAssaultThread.restartGame();
                }
            }
        });

        MissionBox.getBtnGameStartStop().addListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!MissionBox.isGameStartable()) return;
                logger.debug("btnGameStartStop");
                if (farcryAssaultThread.getGameState() == Farcry1AssaultThread.GAME_PRE_GAME) {
                    farcryAssaultThread.startGame();
                } else {
                    farcryAssaultThread.restartGame();
                }
            }
        });

        MissionBox.getBtnMisc().addListener((GpioPinListenerDigital) event -> {


//            fadeout(playWinningSon);


            quitGame();
        });

        MissionBox.getBtnMisc().addListener(new ActionListener() {
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
