package gamemodes;

import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import interfaces.MessageListener;
import main.MissionBox;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashSet;

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

    private String FOREVER = Integer.toString(Integer.MAX_VALUE);

    HashSet<String> timeAnnouncements = new HashSet<>();

    public Farcry1Assault() throws IOException {

        logger.setLevel(MissionBox.getLogLevel());


        MessageListener gameTimeListener = messageEvent -> {
            String thisAnnoucement = messageEvent.getDateTimeFormatted();

//            // Time announcer
//            if (isGameRunning(messageEvent.getMode())) {
//                if (messageEvent.getMode() != Farcry1AssaultThread.GAME_FLAG_HOT && !lastAnnoucement.equals(thisAnnoucement) && MissionBox.getTimeAnnouncements().containsKey(thisAnnoucement)) {
//                    lastAnnoucement = thisAnnoucement;
//
//                    int minutes = messageEvent.getTime().getMinuteOfHour();
//                    int seconds = messageEvent.getTime().getSecondOfMinute();
//
//                    logger.debug("time announcer: " + minutes + ":" + seconds);
//
//                    if (minutes > 0 && minutes <= 5) {
//                        MissionBox.minuteSignal(minutes);
//                    }
//
//                    if (minutes == 0) {
//                        if (seconds > 10) MissionBox.setScheme(MissionBox.MBX_TIME_SIREN, seconds / 10 + ";500,500");
//                        else if (seconds == 10) MissionBox.setScheme(MissionBox.MBX_TIME_SIREN, seconds + ";500,500");
//                    }
//                }
//
//                // Respawn announcer
//                if (MissionBox.isRESPAWN()) {
//                    if (messageEvent.getMode() == Farcry1AssaultThread.GAME_FLAG_HOT || messageEvent.getMode() == Farcry1AssaultThread.GAME_FLAG_COLD) {
//                        MissionBox.setRespawnTimer(Integer.toString(RESPAWNINSECONDS - Seconds.secondsBetween(lastRespawn, new DateTime()).getSeconds()));
//                        if (!messageEvent.getTime().equals(lastRespawn) && Seconds.secondsBetween(lastRespawn, new DateTime()).getSeconds() >= RESPAWNINSECONDS) {
//                            lastRespawn = new DateTime();
//                            MissionBox.setScheme(MissionBox.MBX_RESPAWN_SIREN, "1;2000,0");
//                            logger.info("Respawn...");
//                        }
//                    }
//                }
//            }

            if (messageEvent.getMode() == Farcry1AssaultThread.GAME_PRE_GAME) {
                MissionBox.setTimerMessage("--");
                MissionBox.setRespawnTimer("--");
            } else if (messageEvent.getMode() == Farcry1AssaultThread.GAME_OUTCOME_FLAG_TAKEN) {
                MissionBox.setTimerMessage("Flag taken");
                MissionBox.setRespawnTimer("--");
            } else if (messageEvent.getMode() == Farcry1AssaultThread.GAME_OUTCOME_FLAG_DEFENDED) {
                MissionBox.setTimerMessage("Flag defended");
                MissionBox.setRespawnTimer("--");
            } else {
                MissionBox.setTimerMessage(thisAnnoucement);
            }

        };

        MessageListener percentageListener = messageEvent -> {
            if (messageEvent.getMode() == Farcry1AssaultThread.GAME_FLAG_HOT) {
                MissionBox.setProgress(messageEvent.getPercentage());
                int countdown_index = messageEvent.getPercentage().intValue() / 10;
                if (prev_countdown_index != countdown_index) {
                    prev_countdown_index = countdown_index;
//                    MissionBox.countdown(countdown_index);
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
                MissionBox.setScheme(MissionBox.MBX_LED_GREEN, FOREVER + ";1000,1000");
                MissionBox.off(MissionBox.MBX_LED_RED);


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

                MissionBox.setProgress(new BigDecimal(-1));

                if (prev_countdown_index > -1) {
                    MissionBox.setScheme(MissionBox.MBX_SHUTDOWN_SIREN, "1;1000,0");
                }

                MissionBox.off(MissionBox.MBX_LED_GREEN);
                MissionBox.setScheme(MissionBox.MBX_LED_PB_GREEN, FOREVER + ";350,3000");
                MissionBox.setScheme(MissionBox.MBX_LED_PB_YELLOW, FOREVER + ";350,3000");
                MissionBox.setScheme(MissionBox.MBX_LED_PB_RED, FOREVER + ";350,3000");
                MissionBox.setScheme(MissionBox.MBX_LED_RGB_RED, FOREVER + ";350,3000");

                MissionBox.setScheme(MissionBox.MBX_LED_RED, FOREVER + ";1000,1000");

            } else if (messageEvent.getMode() == Farcry1AssaultThread.GAME_PRE_GAME) {
                /***
                 *      ____            ____
                 *     |  _ \ _ __ ___ / ___| __ _ _ __ ___   ___
                 *     | |_) | '__/ _ \ |  _ / _` | '_ ` _ \ / _ \
                 *     |  __/| | |  __/ |_| | (_| | | | | | |  __/
                 *     |_|   |_|  \___|\____|\__,_|_| |_| |_|\___|
                 *
                 */
                logger.debug("GAME_PRE_GAME");
                gameWon = false;
                prev_countdown_index = -1;

                MissionBox.enableSettings(true);

                MissionBox.off(MissionBox.MBX_LED_RED);
                MissionBox.off(MissionBox.MBX_LED_GREEN);
                MissionBox.off(MissionBox.MBX_SHUTDOWN_SIREN);
                MissionBox.off(MissionBox.MBX_RESPAWN_SIREN);
                MissionBox.off(MissionBox.MBX_AIRSIREN);

                MissionBox.setScheme(MissionBox.MBX_LED_PB_RED, FOREVER + ";1000,2000");
                MissionBox.setScheme(MissionBox.MBX_LED_PB_YELLOW, FOREVER + ";0,1000,1000,1000");
                MissionBox.setScheme(MissionBox.MBX_LED_PB_GREEN, FOREVER + ";0,2000,1000,0");

                MissionBox.setScheme(MissionBox.MBX_LED_RGB_RED, FOREVER + ";1000,2000");
                MissionBox.setScheme(MissionBox.MBX_LED_RGB_BLUE, FOREVER + ";0,1000,1000,1000");
                MissionBox.setScheme(MissionBox.MBX_LED_RGB_GREEN, FOREVER + ";0,2000,1000,0");


            } else if (messageEvent.getMode() == Farcry1AssaultThread.GAME_OUTCOME_FLAG_DEFENDED) {

                logger.debug("GAME_OUTCOME_FLAG_DEFENDED");

                MissionBox.off(MissionBox.MBX_LED_RED);
                MissionBox.off(MissionBox.MBX_LED_GREEN);

                MissionBox.off(MissionBox.MBX_LED_RGB_RED);
                MissionBox.off(MissionBox.MBX_LED_RGB_BLUE);
                MissionBox.setScheme(MissionBox.MBX_LED_GREEN, FOREVER + ";1000,1000");
                MissionBox.setScheme(MissionBox.MBX_LED_RGB_GREEN, FOREVER + ";1000,1000");
                MissionBox.off(MissionBox.MBX_LED_PB_YELLOW);
                MissionBox.off(MissionBox.MBX_LED_PB_RED);


                // the end siren
                MissionBox.setScheme(MissionBox.MBX_AIRSIREN, "1;5000,0");

            } else if (messageEvent.getMode() == Farcry1AssaultThread.GAME_OUTCOME_FLAG_TAKEN) {
                /***
                 *      _____ _            _____     _
                 *     |  ___| | __ _  __ |_   _|_ _| | _____ _ __
                 *     | |_  | |/ _` |/ _` || |/ _` | |/ / _ \ '_ \
                 *     |  _| | | (_| | (_| || | (_| |   <  __/ | | |
                 *     |_|   |_|\__,_|\__, ||_|\__,_|_|\_\___|_| |_|
                 *                    |___/
                 */
                logger.debug("GAME_OUTCOME_FLAG_TAKEN");

                MissionBox.off(MissionBox.MBX_SHUTDOWN_SIREN);
                MissionBox.off(MissionBox.MBX_RESPAWN_SIREN);

                MissionBox.off(MissionBox.MBX_LED_PB_GREEN);
                MissionBox.off(MissionBox.MBX_LED_PB_YELLOW);
                MissionBox.setScheme(MissionBox.MBX_LED_PB_RED, FOREVER + ";500,500");
                MissionBox.setScheme(MissionBox.MBX_LED_RGB_RED, FOREVER + ";500,500");


                MissionBox.off(MissionBox.MBX_LED_RGB_GREEN);
                MissionBox.off(MissionBox.MBX_LED_RGB_BLUE);
                MissionBox.setScheme(MissionBox.MBX_LED_RED, FOREVER + ";1000,1000");
                MissionBox.setScheme(MissionBox.MBX_LED_RGB_RED, FOREVER + ";1000,1000");
                MissionBox.off(MissionBox.MBX_LED_PB_YELLOW);
                MissionBox.off(MissionBox.MBX_LED_PB_GREEN);

            } else if (messageEvent.getMode() == Farcry1AssaultThread.GAME_FLAG_ACTIVE) {
                /***
                 *      _____ _                _        _   _
                 *     |  ___| | __ _  __ _   / \   ___| |_(_)_   _____
                 *     | |_  | |/ _` |/ _` | / _ \ / __| __| \ \ / / _ \
                 *     |  _| | | (_| | (_| |/ ___ \ (__| |_| |\ V /  __/
                 *     |_|   |_|\__,_|\__, /_/   \_\___|\__|_| \_/ \___|
                 *                    |___/
                 */
                logger.debug("GAME_FLAG_ACTIVE");
                MissionBox.enableSettings(false);
                RESPAWNINSECONDS = Integer.parseInt(MissionBox.getConfig().getProperty(MissionBox.FCY_RESPAWN_TIME));
                lastAnnoucement = "";


                MissionBox.off(MissionBox.MBX_LED_PB_GREEN);
                MissionBox.off(MissionBox.MBX_LED_PB_YELLOW);
                MissionBox.off(MissionBox.MBX_LED_PB_RED);
                MissionBox.off(MissionBox.MBX_LED_RGB_GREEN);
                MissionBox.off(MissionBox.MBX_LED_RGB_BLUE);
                MissionBox.off(MissionBox.MBX_LED_RGB_RED);

                lastRespawn = new DateTime();

                //MissionBox.setScheme(MissionBox.MBX_RESPAWN_SIREN, "1;2000,0");

                // the starting siren
                MissionBox.setScheme(MissionBox.MBX_AIRSIREN, "1;5000,0");

            }
        };

        farcryAssaultThread = new Farcry1AssaultThread(messageEvent -> {

        }, gameTimeListener, percentageListener, gameModeListener);

        MissionBox.getBtnRed().addListener((GpioPinListenerDigital) event -> {
            logger.debug(event);
            MissionBox.getFrmTest().setButtonTestLabel("red", event.getState() == PinState.HIGH); // for debugging
            if (event.getState() == PinState.HIGH) {
                logger.debug("RedButton pressed");

                farcryAssaultThread.setFlagHot(true);
            }
        });

        MissionBox.getBtnRed().addListener(e -> {
            farcryAssaultThread.setFlagHot(true);
        });

        MissionBox.getBtnGreen().addListener((GpioPinListenerDigital) event -> {
            logger.debug(event);
            MissionBox.getFrmTest().setButtonTestLabel("green", event.getState() == PinState.HIGH); // for debugging
            if (event.getState() == PinState.HIGH) {
                logger.debug("GPIO GreenButton down");

                // If both buttons are pressed, the red one wins.
                if (MissionBox.getBtnRed().isHigh())
                    return;

                farcryAssaultThread.setFlagHot(false);
            }
        });

        MissionBox.getBtnGreen().addListener(e -> {
            logger.debug("GreenButton clicked");
            farcryAssaultThread.setFlagHot(false);
        });

        MissionBox.getBtnGameStartStop().addListener((GpioPinListenerDigital) event -> {
            MissionBox.getFrmTest().setButtonTestLabel("start", event.getState() == PinState.HIGH); // for debugging
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

        MissionBox.getBtnGameStartStop().addListener(e -> {
            if (!MissionBox.isGameStartable()) return;
            logger.debug("btnGameStartStop");
            if (farcryAssaultThread.getGameState() == Farcry1AssaultThread.GAME_PRE_GAME) {
                farcryAssaultThread.startGame();
            } else {
                farcryAssaultThread.restartGame();
            }
        });

        MissionBox.getBtnPAUSE().addListener(e -> {
            logger.debug("btnPause - on Screen");

            if (farcryAssaultThread.isPaused()) {
                farcryAssaultThread.resume();
            } else if (farcryAssaultThread.isGameRunning()) {
                farcryAssaultThread.pause();

            }

        });

        MissionBox.getBtnPAUSE().addListener((GpioPinListenerDigital) event -> {
            MissionBox.getFrmTest().setButtonTestLabel("undo", event.getState() == PinState.HIGH); // for debugging
            if (event.getState() == PinState.HIGH) {
                logger.debug("btnPause - GPIO");
                if (farcryAssaultThread.isPaused()) {
                    farcryAssaultThread.resume();
                } else if (farcryAssaultThread.isGameRunning()) {
                    farcryAssaultThread.pause();

                }
            }
            //quitGame();
        });

        MissionBox.getBtnMisc().addListener(e -> quitGame());
        farcryAssaultThread.run();
    }


    @Override
    public void quitGame() {
        MissionBox.shutdownEverything();
        System.exit(0);
    }

}
