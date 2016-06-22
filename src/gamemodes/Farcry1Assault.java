package gamemodes;

import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import interfaces.MessageEvent;
import interfaces.MessageListener;
import main.MissionBox;
import misc.Tools;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Seconds;

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
    private int prev_countdown_index;
    private String lastAnnoucement = "";
    private DateTime lastRespawn = new DateTime();
    private int RESPAWNINSECONDS = 55;

    public Farcry1Assault() throws IOException {


        logger.setLevel(MissionBox.getLogLevel());

//        MessageListener textListener = messageEvent -> logger.debug(messageEvent.getMessage().toString());

        MessageListener gameTimeListener = messageEvent -> {
            String thisAnnoucement = messageEvent.getDateTimeFormatted();

            // Time announcer
            if (isGameRunning(messageEvent.getMode())) {
                if (messageEvent.getMode() != Farcry1AssaultThread.GAME_FLAG_HOT && !lastAnnoucement.equals(thisAnnoucement) && MissionBox.getTimeAnnouncements().containsKey(thisAnnoucement)) {
                    lastAnnoucement = thisAnnoucement;

                    if (MissionBox.isSOUND()) MissionBox.getTimeAnnouncements().get(thisAnnoucement).play();

                    int minutes = messageEvent.getTime().getMinuteOfHour();
                    int seconds = messageEvent.getTime().getSecondOfMinute();
                    if (minutes > 0 && minutes <= 5) {
                        MissionBox.blink("timeSignal", 1000, minutes * 1000 + 300);  // blinks in the number of minutes. the +300ms is just a little more.
                    }

                    if (minutes == 0) {
                        MissionBox.blink("timeSignal", 500, seconds * 500 + 300);  // blinks in the number of minutes. the +300ms is just a little more.
                    }

                }

                // Respawn announcer
                if (messageEvent.getMode() == Farcry1AssaultThread.GAME_FLAG_HOT || messageEvent.getMode() == Farcry1AssaultThread.GAME_FLAG_COLD) {
                    MissionBox.setRespawnTimer(Integer.toString(RESPAWNINSECONDS - Seconds.secondsBetween(lastRespawn, new DateTime()).getSeconds()));
                    if (!messageEvent.getTime().equals(lastRespawn) && Seconds.secondsBetween(lastRespawn, new DateTime()).getSeconds() >= RESPAWNINSECONDS) {
                        lastRespawn = new DateTime();
                        MissionBox.play("minions");
                        MissionBox.blink("respawnSiren", 2000, 2000);
                        logger.info("Respawn...");
                    }
                }
            }

            if (messageEvent.getMode() == Farcry1AssaultThread.GAME_PRE_GAME) {
                MissionBox.setTimerMessage("--");
                MissionBox.setRespawnTimer("--");
            } else if (messageEvent.getMode() == Farcry1AssaultThread.GAME_OVER) {
                MissionBox.setTimerMessage(gameWon ? "Flag taken" : "Flag defended");
                MissionBox.setRespawnTimer("--");
            } else {
                MissionBox.setTimerMessage(thisAnnoucement);
            }

        };

        MessageListener percentageListener = messageEvent -> {
//            logger.debug(messageEvent.getPercentage() + " %");

            if (messageEvent.getMode() == Farcry1AssaultThread.GAME_FLAG_HOT) {
                MissionBox.setProgress(messageEvent.getPercentage());
            }


            if (messageEvent.getMode() == Farcry1AssaultThread.GAME_FLAG_HOT) {
                int countdown_index = messageEvent.getPercentage().intValue() / 10;
                if (prev_countdown_index != countdown_index) {
                    prev_countdown_index = countdown_index;
                    MissionBox.countdown(countdown_index);
                }
            }
        };

        MessageListener gameModeListener = messageEvent -> {
            MissionBox.setMessage(Farcry1AssaultThread.GAME_MODES[messageEvent.getMode()]);
            MissionBox.setGamemode(messageEvent.getMode());

            if (messageEvent.getMode() == Farcry1AssaultThread.GAME_FLAG_HOT) {

                MissionBox.stop("shutdown");
                MissionBox.play("siren", true);
                MissionBox.blink("ledGreen", 100, PinState.HIGH);
                MissionBox.blink("ledRed", 0);


//                MissionBox.blink("flagSiren", 1000);
            } else if (messageEvent.getMode() == Farcry1AssaultThread.GAME_FLAG_COLD) {
                MissionBox.stop("siren");
                MissionBox.setProgress(BigDecimal.ZERO);

                if (prev_countdown_index > -1) {
//                    MissionBox.blink("flagSiren", 0);
                    MissionBox.play("shutdown"); // plays only once when the flag has been touched during this round.
                    MissionBox.blink("shutdownSiren", 1000, 1000);
                }

                MissionBox.blink("ledRed", 1000, PinState.HIGH);
                MissionBox.blink("ledGreen", 0);
            } else if (messageEvent.getMode() == Farcry1AssaultThread.GAME_ROCKET_LAUNCHED) {
                MissionBox.stop("siren");
                MissionBox.setProgress(BigDecimal.ZERO);
                MissionBox.play("rocket");

                MissionBox.blink("respawnSiren", 5000, 5000); // produces a high pitched airraid siren sound by a motor siren

                MissionBox.blink("ledRed", 50, PinState.HIGH);
                MissionBox.blink("ledGreen", 50, PinState.LOW);
                MissionBox.blink("ledBarGreen", 50);
                MissionBox.blink("ledBarYellow", 50);
                MissionBox.blink("ledBarRed", 50);
                gameWon = true;
            } else if (messageEvent.getMode() == Farcry1AssaultThread.GAME_PRE_GAME) {
                gameWon = false;
                prev_countdown_index = -1;

                MissionBox.stopAllSongs();
                MissionBox.enableSettings(true);

                MissionBox.blink("ledRed", 500, PinState.HIGH);
                MissionBox.blink("ledGreen", 500, PinState.LOW);
                MissionBox.blink("ledBarGreen", 1000);
                MissionBox.blink("ledBarYellow", 1000);
                MissionBox.blink("ledBarRed", 1000);

                MissionBox.blink("flagSiren", 0);
                MissionBox.blink("shutdownSiren", 0);
                MissionBox.blink("respawnSiren", 0);

                MissionBox.stop("siren");
                MissionBox.stop("rocket");
                MissionBox.play("welcome");

            } else if (messageEvent.getMode() == Farcry1AssaultThread.GAME_OVER) {
                MissionBox.stop("siren");
                MissionBox.stop("rocket");

                MissionBox.blink("ledGreen", 0);
                MissionBox.blink("ledRed", 0);

                if (gameWon) {
                    MissionBox.play("victory");
                    MissionBox.playWinner();
                    MissionBox.blink("ledBarGreen", 1000);
                    MissionBox.blink("ledBarYellow", 0);
                    MissionBox.blink("ledBarRed", 0);

                } else {
                    MissionBox.play("defeat");
                    MissionBox.playLooser();
                    MissionBox.blink("ledBarGreen", 0);
                    MissionBox.blink("ledBarYellow", 0);
                    MissionBox.blink("ledBarRed", 1000);

                    MissionBox.blink("respawnSiren", 0);
                    MissionBox.blink("shutdownSiren", 5000, 5000);
                }
            } else if (messageEvent.getMode() == Farcry1AssaultThread.GAME_OUTCOME_FLAG_TAKEN) {

                MissionBox.blink("flagSiren", 0);
                MissionBox.blink("shutdownSiren", 0);
                MissionBox.blink("respawnSiren", 0);

                MissionBox.blink("ledRed", 500, PinState.HIGH);
                MissionBox.blink("ledGreen", 500, PinState.LOW);
                MissionBox.blink("ledBarGreen", 500);
                MissionBox.blink("ledBarYellow", 500);
                MissionBox.blink("ledBarRed", 500);

                MissionBox.stop("siren");
                MissionBox.stop("rocket");

            } else if (messageEvent.getMode() == Farcry1AssaultThread.GAME_FLAG_ACTIVE) {
                MissionBox.enableSettings(false);
                RESPAWNINSECONDS = Integer.parseInt(MissionBox.getConfig().getProperty(MissionBox.FCY_RESPAWN));
                lastAnnoucement = "";
                MissionBox.blink("ledBarGreen", 0);
                MissionBox.blink("ledBarYellow", 0);
                MissionBox.blink("ledBarRed", 0);
                lastRespawn = new DateTime();
                MissionBox.play("minions");
                MissionBox.blink("respawnSiren", 2000, 2000);
            }
        };

        farcryAssaultThread = new Farcry1AssaultThread(new MessageListener() {
            @Override
            public void messageReceived(MessageEvent messageEvent) {

            }
        }, gameTimeListener, percentageListener, gameModeListener);

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
                if (MissionBox.getBtnRed().isHigh() || MissionBox.getGamemode() != Farcry1AssaultThread.GAME_FLAG_HOT)
                    return;
                logger.debug("GreenButton pressed");
                farcryAssaultThread.setFlag(false);
            }
        });

        MissionBox.getBtnGreen().addListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (MissionBox.getBtnRed().isHigh() || MissionBox.getGamemode() != Farcry1AssaultThread.GAME_FLAG_HOT)
                    return;
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

    boolean isGameRunning(int mode) {
        return mode == Farcry1AssaultThread.GAME_FLAG_ACTIVE || mode == Farcry1AssaultThread.GAME_FLAG_HOT || mode == Farcry1AssaultThread.GAME_FLAG_COLD;
    }

    @Override
    public void quitGame() {
        System.exit(0);
    }

}
