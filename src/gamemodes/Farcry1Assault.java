package gamemodes;

import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import interfaces.MessageEvent;
import interfaces.MessageListener;
import main.MissionBox;
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
    private boolean firstStart = true;
    private boolean RESPAWN = false;
    private String FOREVER = Integer.toString(Integer.MAX_VALUE);

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
                    int seconds = messageEvent.getTime().getSecondOfMinute() / 10;

                    logger.debug("time announcer: " + minutes + ":" + seconds);

                    if (minutes > 0 && minutes <= 5) {
                        MissionBox.minuteSignal(minutes);
                    }

                    if (minutes == 0) {
                        MissionBox.secondsSignal(seconds);
                    }

                }

                // Respawn announcer
                if (RESPAWN) {
                    if (messageEvent.getMode() == Farcry1AssaultThread.GAME_FLAG_HOT || messageEvent.getMode() == Farcry1AssaultThread.GAME_FLAG_COLD) {
                        MissionBox.setRespawnTimer(Integer.toString(RESPAWNINSECONDS - Seconds.secondsBetween(lastRespawn, new DateTime()).getSeconds()));
                        if (!messageEvent.getTime().equals(lastRespawn) && Seconds.secondsBetween(lastRespawn, new DateTime()).getSeconds() >= RESPAWNINSECONDS) {
                            lastRespawn = new DateTime();
                            MissionBox.play("minions");
                            MissionBox.setScheme("respawnSiren", "1;2000,0");
                            logger.info("Respawn...");
                        }
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
            if (messageEvent.getMode() == Farcry1AssaultThread.GAME_FLAG_HOT) {
//                // kickstart a little
//                BigDecimal percentage = messageEvent.getPercentage().add(BigDecimal.ONE);
                MissionBox.setProgress(messageEvent.getPercentage());
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
                /***
                 *      _____ _             _   _       _
                 *     |  ___| | __ _  __ _| | | | ___ | |_
                 *     | |_  | |/ _` |/ _` | |_| |/ _ \| __|
                 *     |  _| | | (_| | (_| |  _  | (_) | |_
                 *     |_|   |_|\__,_|\__, |_| |_|\___/ \__|
                 *                    |___/
                 */
                logger.debug("GAME_FLAG_HOT");
                MissionBox.stop("shutdown");
                MissionBox.play("siren", true);
                MissionBox.setScheme("ledGreen", FOREVER + ";1000,1000");
                MissionBox.off("ledRed");


            } else if (messageEvent.getMode() == Farcry1AssaultThread.GAME_FLAG_COLD) {
                /***
                 *      _____ _              ____      _     _
                 *     |  ___| | __ _  __ _ / ___|___ | | __| |
                 *     | |_  | |/ _` |/ _` | |   / _ \| |/ _` |
                 *     |  _| | | (_| | (_| | |__| (_) | | (_| |
                 *     |_|   |_|\__,_|\__, |\____\___/|_|\__,_|
                 *                    |___/
                 */
                logger.debug("GAME_FLAG_COLD");
                MissionBox.stop("siren");
                MissionBox.setProgress(new BigDecimal(-1));

                if (prev_countdown_index > -1) {
                    MissionBox.play("shutdown"); // plays only once when the flag has been touched during this round.
                    MissionBox.setScheme("shutdownSiren", "1;1000,0");
                }

                MissionBox.off("ledGreen");
                MissionBox.off("ledBarGreen");
                MissionBox.off("ledBarYellow");
                MissionBox.off("ledBarRed");
                MissionBox.setScheme("ledRed", FOREVER + ";1000,1000");

            } else if (messageEvent.getMode() == Farcry1AssaultThread.GAME_ROCKET_LAUNCHED) {
                logger.debug("GAME_ROCKET_LAUNCHED");
                MissionBox.stop("siren");
                MissionBox.setProgress(new BigDecimal(-1));
                MissionBox.play("rocket");

                MissionBox.setScheme("rocketlaunched", "1;3000,0"); // produces a high pitched airraid siren sound by a motor siren

                MissionBox.off("ledRed");
                MissionBox.off("ledGreen");
                MissionBox.setScheme("ledBarGreen", FOREVER + ";50,50");
                MissionBox.setScheme("ledBarYellow", FOREVER + ";50,50");
                MissionBox.setScheme("ledBarRed", FOREVER + ";50,50");

                gameWon = true;
            } else if (messageEvent.getMode() == Farcry1AssaultThread.GAME_PRE_GAME) {
                logger.debug("GAME_PRE_GAME");
                gameWon = false;
                prev_countdown_index = -1;

                MissionBox.stopAllSongs();
                MissionBox.enableSettings(true);


                MissionBox.off("ledRed");
                MissionBox.off("ledGreen");
                MissionBox.off("shutdownSiren");
                MissionBox.off("respawnSiren");

                MissionBox.setScheme("ledBarGreen", FOREVER + ";1000,1000");
                MissionBox.setScheme("ledBarYellow", FOREVER + ";1000,1000");
                MissionBox.setScheme("ledBarRed", FOREVER + ";1000,1000");

                MissionBox.stop("siren");
                MissionBox.stop("rocket");

                if (firstStart) {
                    firstStart = false;
                    MissionBox.play("tranquility");
                }

            } else if (messageEvent.getMode() == Farcry1AssaultThread.GAME_OVER) {
                logger.debug("GAME_OVER");
                MissionBox.stop("siren");
                MissionBox.stop("rocket");

                MissionBox.off("ledRed");
                MissionBox.off("ledGreen");

                if (gameWon) {
                    MissionBox.play("victory");
                    MissionBox.playWinner();
                    MissionBox.setScheme("ledBarRed", FOREVER + ";1000,1000");
                    MissionBox.off("ledBarYellow");
                    MissionBox.off("ledBarGreen");
                } else {
                    MissionBox.play("defeat");
                    MissionBox.playLooser();
                    MissionBox.setScheme("ledBarGreen", FOREVER + ";1000,1000");
                    MissionBox.off("ledBarYellow");
                    MissionBox.off("ledBarRed");
                    MissionBox.setScheme("shutdownSiren", "1;5000,0");
                }
            } else if (messageEvent.getMode() == Farcry1AssaultThread.GAME_OUTCOME_FLAG_TAKEN) {

                logger.debug("GAME_OUTCOME_FLAG_TAKEN");

                MissionBox.off("shutdownSiren");
                MissionBox.off("respawnSiren");

                MissionBox.setScheme("ledBarGreen", FOREVER+";500,500");
                MissionBox.setScheme("ledBarYellow", FOREVER+";500,500");
                MissionBox.setScheme("ledBarRed", FOREVER+";500,500");

                MissionBox.stop("siren");
                MissionBox.stop("rocket");

            } else if (messageEvent.getMode() == Farcry1AssaultThread.GAME_FLAG_ACTIVE) {
                logger.debug("GAME_FLAG_ACTIVE");
                MissionBox.stop("tranquility");
                MissionBox.stopAllSongs();
                MissionBox.enableSettings(false);
                RESPAWNINSECONDS = Integer.parseInt(MissionBox.getConfig().getProperty(MissionBox.FCY_RESPAWN));
                lastAnnoucement = "";
                MissionBox.off("ledBarGreen");
                MissionBox.off("ledBarYellow");
                MissionBox.off("ledBarRed");
                lastRespawn = new DateTime();

                MissionBox.play("minions");
                MissionBox.setScheme("respawnSiren", "1;2000,0");

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
                logger.debug("GPIO GreenButton down");
                // If both buttons are pressed, the red one wins.
                if (MissionBox.getBtnRed().isLow() || MissionBox.getGamemode() != Farcry1AssaultThread.GAME_FLAG_HOT)
                    return;
                logger.debug("GreenButton pressed");
                farcryAssaultThread.setFlag(false);
            }
        });

        MissionBox.getBtnGreen().addListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logger.debug("GUI GreenButton down");
                if (MissionBox.getBtnRed().isLow() || MissionBox.getGamemode() != Farcry1AssaultThread.GAME_FLAG_HOT)
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

        MissionBox.getBtnUndo().addListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logger.debug("btnUndo");
                farcryAssaultThread.undo();
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
        MissionBox.shutdownEverything();
        System.exit(0);
    }

}
