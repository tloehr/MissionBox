package gamemodes;

import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import interfaces.FC1DetailsMessageEvent;
import interfaces.MessageListener;
import main.MissionBox;
import misc.Tools;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.Seconds;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by tloehr on 31.05.15.
 */
public class Farcry1Assault implements GameMode {
    private final Logger logger = Logger.getLogger(getClass());
    private boolean gameJustStarted;
    private Farcry1AssaultThread farcryAssaultThread;
    private String FOREVER = Integer.toString(Integer.MAX_VALUE);
    //    private String lastAnnoucement = "";
    private int lastAnnouncedMinute = -1;


    // das mach ich nur, weil ich diese beiden Flags als finals brauche.
    private final AtomicBoolean coldcountdownrunning, hotcountdownrunning;

    /**
     * wird gebraucht, wenn während eines PREGAMES die Zeiten geändert werden
     *
     * @param maxgametime
     */
    public void setMaxgametime(long maxgametime) {
        farcryAssaultThread.setMaxgametime(maxgametime);
    }

    public void setRespawninterval(long respawninterval) {
        farcryAssaultThread.setRespawninterval(respawninterval);
    }

    /**
     * wird gebraucht, wenn während eines PREGAMES die Zeiten geändert werden
     *
     * @param capturetime
     */
    public void setCapturetime(long capturetime) {
        farcryAssaultThread.setCapturetime(capturetime);
    }


    /**
     * Diese ganze Klasse besteht eigentlich nur auf einem riesigen Konstruktor,
     * der alle Listener für den eigentlichen Thread erstellt.
     * Diese Listener sind es dann, die die Reaktionen der Box auf die Ereignisse innerhalb
     * des Threads steuern.
     *
     * @throws IOException
     */
    public Farcry1Assault() throws IOException {

        logger.setLevel(MissionBox.getLogLevel());

        logger.info("\n" +
                "      ____  _             _   _               _____           ____                 _                        _ _   \n" +
                "     / ___|| |_ __ _ _ __| |_(_)_ __   __ _  |  ___|_ _ _ __ / ___|_ __ _   _     / \\   ___ ___  __ _ _   _| | |_ \n" +
                "     \\___ \\| __/ _` | '__| __| | '_ \\ / _` | | |_ / _` | '__| |   | '__| | | |   / _ \\ / __/ __|/ _` | | | | | __|\n" +
                "      ___) | || (_| | |  | |_| | | | | (_| | |  _| (_| | |  | |___| |  | |_| |  / ___ \\\\__ \\__ \\ (_| | |_| | | |_ \n" +
                "     |____/ \\__\\__,_|_|   \\__|_|_| |_|\\__, | |_|  \\__,_|_|   \\____|_|   \\__, | /_/   \\_\\___/___/\\__,_|\\__,_|_|\\__|\n" +
                "                                      |___/                             |___/                                     \n" +
                "");

        coldcountdownrunning = new AtomicBoolean(false);
        hotcountdownrunning = new AtomicBoolean(false);

        MessageListener gameTimeListener = messageEvent -> {


//            logger.debug(messageEvent);

//            String thisAnnoucement = messageEvent.getDateTimeFormatted();

            // Time announcer


            /***
             *      ____                                        ____  _                   _
             *     |  _ \ ___  ___ _ __   __ ___      ___ __   / ___|(_) __ _ _ __   __ _| |
             *     | |_) / _ \/ __| '_ \ / _` \ \ /\ / / '_ \  \___ \| |/ _` | '_ \ / _` | |
             *     |  _ <  __/\__ \ |_) | (_| |\ V  V /| | | |  ___) | | (_| | | | | (_| | |
             *     |_| \_\___||___/ .__/ \__,_| \_/\_/ |_| |_| |____/|_|\__, |_| |_|\__,_|_|
             *                    |_|                                   |___/
             */
            if (Long.parseLong(MissionBox.getConfig(MissionBox.FCY_RESPAWN_INTERVAL)) > 0l) {
                if (messageEvent.getGameState() == Farcry1AssaultThread.GAME_FLAG_HOT || messageEvent.getGameState() == Farcry1AssaultThread.GAME_FLAG_COLD) {

                    FC1DetailsMessageEvent event = (FC1DetailsMessageEvent) messageEvent;
                    String respawnTimer = Tools.formatLongTime(event.getLastrespawn() + event.getRespawninterval() - event.getGametimer(), "mm:ss");
                    MissionBox.setRespawnTimer(respawnTimer);
//                    logger.debug(event.getLastrespawn() + ", " + event.getRespawninterval() + " , " + event.getGametimer());
                    if (event.getLastrespawn() + event.getRespawninterval() <= event.getGametimer()) {
                        MissionBox.setScheme(MissionBox.MBX_RESPAWN_SIREN, "1;%d,0", MissionBox.getIntConfig(MissionBox.MBX_RESPAWN_SIRENTIME));
                        logger.info("\n" +
                                "  ____                                      _             \n" +
                                " |  _ \\ ___  ___ _ __   __ ___      ___ __ (_)_ __   __ _ \n" +
                                " | |_) / _ \\/ __| '_ \\ / _` \\ \\ /\\ / / '_ \\| | '_ \\ / _` |\n" +
                                " |  _ <  __/\\__ \\ |_) | (_| |\\ V  V /| | | | | | | | (_| |\n" +
                                " |_| \\_\\___||___/ .__/ \\__,_| \\_/\\_/ |_| |_|_|_| |_|\\__, |\n" +
                                "                |_|                                 |___/ ");
                    }
                }
            }

            /***
             *       ____                  _      _                       ____  _                   _
             *      / ___|___  _   _ _ __ | |_ __| | _____      ___ __   / ___|(_) __ _ _ __   __ _| |
             *     | |   / _ \| | | | '_ \| __/ _` |/ _ \ \ /\ / / '_ \  \___ \| |/ _` | '_ \ / _` | |
             *     | |__| (_) | |_| | | | | || (_| | (_) \ V  V /| | | |  ___) | | (_| | | | | (_| | |
             *      \____\___/ \__,_|_| |_|\__\__,_|\___/ \_/\_/ |_| |_| |____/|_|\__, |_| |_|\__,_|_|
             *                                                                    |___/
             */
            if (messageEvent.getGameState() == Farcry1AssaultThread.GAME_FLAG_HOT) {
                /***
                 *      _   _  ___ _____
                 *     | | | |/ _ \_   _|
                 *     | |_| | | | || |
                 *     |  _  | |_| || |
                 *     |_| |_|\___/ |_|
                 *
                 */
                FC1DetailsMessageEvent event = (FC1DetailsMessageEvent) messageEvent;

                if (!hotcountdownrunning.get()) {
                    Interval remaining = new Interval(event.getGametimer(), event.getTimeWhenTheFlagWasActivated() + event.getCapturetime());
                    if (Seconds.secondsIn(remaining).getSeconds() < 10) { // die letzten 10 Sekunden laufen
                        hotcountdownrunning.set(true);
                        MissionBox.setScheme(MissionBox.MBX_SIREN1, "10;500,500"); // eine Sekunde mehr. Dann gibts nicht so eine Lücke beim Ende
                    }
                }
            } else if (messageEvent.getGameState() == Farcry1AssaultThread.GAME_FLAG_COLD) {
                /***
                 *       ____ ___  _     ____
                 *      / ___/ _ \| |   |  _ \
                 *     | |  | | | | |   | | | |
                 *     | |__| |_| | |___| |_| |
                 *      \____\___/|_____|____/
                 *
                 */
                FC1DetailsMessageEvent event = (FC1DetailsMessageEvent) messageEvent;

                if (!event.isOvertime()) { // Sonst gibts eine Exception beim Interval (gametimer > maxgametime)
                    // Spielt bei HOT keine Rolle.
                    if (!coldcountdownrunning.get()) {
                        Interval remaining = new Interval(event.getGametimer(), event.getMaxgametime());
                        if (Seconds.secondsIn(remaining).getSeconds() < 10) { // die letzten 10 Sekunden laufen
                            coldcountdownrunning.set(true);
                            MissionBox.setScheme(MissionBox.MBX_SHUTDOWN_SIREN, "10;500,500"); // eine Sekunde mehr. Dann gibts nicht so eine Lücke beim Ende
                        }
                    }
                }
            }


            if (messageEvent.getGameState() == Farcry1AssaultThread.GAME_PRE_GAME) {

                MissionBox.setTimerMessage("--");
                MissionBox.setMessage(Tools.h1(Tools.xx("fc1assault.gamestate." + Farcry1AssaultThread.GAMSTATS[messageEvent.getGameState()])));
                MissionBox.setRespawnTimer("--");
            } else if (messageEvent.getGameState() == Farcry1AssaultThread.GAME_OUTCOME_FLAG_TAKEN) {

                MissionBox.setRespawnTimer("--");
                MissionBox.setMessage(Tools.h1(Tools.xx("fc1assault.gamestate." + Farcry1AssaultThread.GAMSTATS[messageEvent.getGameState()])));
                MissionBox.setRespawnTimer("--");
            } else if (messageEvent.getGameState() == Farcry1AssaultThread.GAME_OUTCOME_FLAG_DEFENDED) {

                MissionBox.setRespawnTimer("--");
                String message = Tools.h1(Tools.xx("fc1assault.gamestate." + Farcry1AssaultThread.GAMSTATS[messageEvent.getGameState()]));

//                // todo: das hier erscheint immer
//                if (((FC1DetailsMessageEvent) messageEvent).isOvertime()) {
//                    message += "<h2>SUDDEN DEATH (Overtime)</h2>";
//                }

                MissionBox.setMessage(message);
                MissionBox.setRespawnTimer("--");
            } else if (messageEvent.getGameState() == Farcry1AssaultThread.GAME_FLAG_HOT) {
                MissionBox.setTimerMessage(((FC1DetailsMessageEvent) messageEvent).toHTML());
                long remain = farcryAssaultThread.getRemaining();

                String message = Tools.h1(Tools.xx("fc1assault.gamestate." + Farcry1AssaultThread.GAMSTATS[messageEvent.getGameState()]) + " " + Tools.formatLongTime(remain, "mm:ss"));

                if (((FC1DetailsMessageEvent) messageEvent).isOvertime()) {
                    message += "<h2> OVERTIME: " + Tools.formatLongTime(((FC1DetailsMessageEvent) messageEvent).getOvertime(), "mm:ss") + "</h2>";
                }


                MissionBox.setMessage(message);
                MissionBox.setProgress(((FC1DetailsMessageEvent) messageEvent).getTimeWhenTheFlagWasActivated(), ((FC1DetailsMessageEvent) messageEvent).getGametimer(), ((FC1DetailsMessageEvent) messageEvent).getTimeWhenTheFlagWasActivated() + ((FC1DetailsMessageEvent) messageEvent).getCapturetime());

            } else if (messageEvent.getGameState() == Farcry1AssaultThread.GAME_FLAG_COLD) {
                MissionBox.setTimerMessage(((FC1DetailsMessageEvent) messageEvent).toHTML());
                long remain = farcryAssaultThread.getRemaining();
                DateTime remainingTime = new DateTime(remain, DateTimeZone.UTC);

                int minutes = remainingTime.getMinuteOfHour();
                int seconds = remainingTime.getSecondOfMinute();


                if (lastAnnouncedMinute != minutes) {
                    lastAnnouncedMinute = minutes;
                    logger.debug("time announcer: " + minutes + ":" + seconds);
                    if (minutes > 0) {
                        String scheme = "";
                        for (int m = 1; m < minutes; m++) {
                            scheme += "250,250,";
                        }

                        scheme += "250,10000";

                        MissionBox.setScheme(MissionBox.MBX_LED_PROGRESS1_GREEN, "10000;" + scheme);
                        MissionBox.setScheme(MissionBox.MBX_LED_PROGRESS2_GREEN, "10000;" + scheme);
                    } else {
                        MissionBox.off(MissionBox.MBX_LED_PROGRESS1_GREEN);
                        MissionBox.off(MissionBox.MBX_LED_PROGRESS2_GREEN);
                    }
                }
                
                String message = Tools.h1(Tools.xx("fc1assault.gamestate." + Farcry1AssaultThread.GAMSTATS[messageEvent.getGameState()]) + " " + Tools.formatLongTime(remain, "mm:ss"));
                MissionBox.setMessage(message);
            } else if (messageEvent.getGameState() == Farcry1AssaultThread.GAME_PAUSING) {
                MissionBox.setTimerMessage(((FC1DetailsMessageEvent) messageEvent).toHTML());
                long pausingsince = System.currentTimeMillis() - ((FC1DetailsMessageEvent) messageEvent).getPausingSince();
                String message = Tools.h1(Tools.xx("fc1assault.gamestate." + Farcry1AssaultThread.GAMSTATS[messageEvent.getGameState()]) + " " + Tools.formatLongTime(pausingsince, "mm:ss"));
                MissionBox.setMessage(message);
            } else if (messageEvent.getGameState() == Farcry1AssaultThread.GAME_GOING_TO_RESUME) {
                MissionBox.setTimerMessage(((FC1DetailsMessageEvent) messageEvent).toHTML());
                long resumein = ((FC1DetailsMessageEvent) messageEvent).getResumingSince() + ((FC1DetailsMessageEvent) messageEvent).getResumeinterval() - System.currentTimeMillis() + 1000; // 1 sekunde drauf, weg der Anzeige
                String message = Tools.h1(Tools.xx("fc1assault.gamestate." + Farcry1AssaultThread.GAMSTATS[messageEvent.getGameState()]) + " " + Tools.formatLongTime(resumein, "mm:ss"));
                MissionBox.setMessage(message);
            } else {
                MissionBox.setTimerMessage("Don't know");
                MissionBox.setRespawnTimer("--");
            }

        };

        MessageListener gameModeListener = messageEvent -> {

            if (messageEvent.getGameState() == Farcry1AssaultThread.GAME_FLAG_HOT) {
                /***
                 *      _____ _             _   _       _
                 *     |  ___| | __ _  __ _| | | | ___ | |_
                 *     | |_  | |/ _` |/ _` | |_| |/ _ \| __|
                 *     |  _| | | (_| | (_| |  _  | (_) | |_
                 *     |_|   |_|\__,_|\__, |_| |_|\___/ \__|
                 *                    |___/
                 */
                logger.debug("GAME_FLAG_HOT");
                MissionBox.setScheme(MissionBox.MBX_LED1_BTN_GREEN, FOREVER + ";1000,1000");
                MissionBox.off(MissionBox.MBX_LED1_BTN_RED);
                MissionBox.setScheme(MissionBox.MBX_LED2_BTN_GREEN, FOREVER + ";1000,1000");
                MissionBox.off(MissionBox.MBX_LED2_BTN_RED);

                // anders rum (bei cold) brauchen wir das nicht, weil diese sirene über das Percentage Interface abgeschaltet wird.
                if (coldcountdownrunning.get()) MissionBox.off(MissionBox.MBX_SHUTDOWN_SIREN);

                lastAnnouncedMinute = -1;
                hotcountdownrunning.set(false);
                coldcountdownrunning.set(false);

                // die hauptsirene wird hier nicht aktiviert, weil sie über das PercentageInterface läuft.

            } else if (messageEvent.getGameState() == Farcry1AssaultThread.GAME_FLAG_COLD) {
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

                MissionBox.off(MissionBox.MBX_LED1_BTN_GREEN);
                MissionBox.off(MissionBox.MBX_LED2_BTN_GREEN);

                MissionBox.off(MissionBox.MBX_LED_PROGRESS1_RED);
                MissionBox.off(MissionBox.MBX_LED_PROGRESS1_YELLOW);
                MissionBox.off(MissionBox.MBX_LED_PROGRESS1_GREEN);

                MissionBox.off(MissionBox.MBX_LED_PROGRESS2_RED);
                MissionBox.off(MissionBox.MBX_LED_PROGRESS2_YELLOW);
                MissionBox.off(MissionBox.MBX_LED_PROGRESS2_GREEN);

                MissionBox.setScheme(MissionBox.MBX_LED1_BTN_RED, FOREVER + ";1000,1000");
                MissionBox.setScheme(MissionBox.MBX_LED2_BTN_RED, FOREVER + ";1000,1000");


                // LED Anzeige, welche die Langeweile der Verteidiger ausdrückt.


                // damit beim Anfang nicht direkt die Shutdown Sirene ertönt
                // UND damit bei einem Overtime die End-Sirene und die Shutdown-Sirene nicht kollidieren
                if (!gameJustStarted && !((FC1DetailsMessageEvent) messageEvent).isOvertime()) {
                    MissionBox.setScheme(MissionBox.MBX_SHUTDOWN_SIREN, "1;1000,0");
                }

                gameJustStarted = false;
                lastAnnouncedMinute = -1;
                hotcountdownrunning.set(false);
                coldcountdownrunning.set(false);
            } else if (messageEvent.getGameState() == Farcry1AssaultThread.GAME_PRE_GAME) {
                /***
                 *      ____            ____
                 *     |  _ \ _ __ ___ / ___| __ _ _ __ ___   ___
                 *     | |_) | '__/ _ \ |  _ / _` | '_ ` _ \ / _ \
                 *     |  __/| | |  __/ |_| | (_| | | | | | |  __/
                 *     |_|   |_|  \___|\____|\__,_|_| |_| |_|\___|
                 *
                 */
                logger.debug("GAME_PRE_GAME");

                MissionBox.enableSettings(true);
                MissionBox.getFrmTest().getBtnClearEvent().setEnabled(false);

                MissionBox.setProgress(BigDecimal.ONE.negate()); // stop progess. -1 beendet alles.


                MissionBox.off(MissionBox.MBX_SHUTDOWN_SIREN);
                MissionBox.off(MissionBox.MBX_AIRSIREN);
                MissionBox.off(MissionBox.MBX_SIREN1);
                MissionBox.off(MissionBox.MBX_RESPAWN_SIREN);

                MissionBox.off(MissionBox.MBX_RESPAWN_SIREN);

                MissionBox.setScheme(MissionBox.MBX_LED1_BTN_RED, FOREVER + ";1000,1000");
                MissionBox.setScheme(MissionBox.MBX_LED1_BTN_GREEN, FOREVER + ";0,1000,1000,1000");

                MissionBox.setScheme(MissionBox.MBX_LED2_BTN_RED, FOREVER + ";1000,1000");
                MissionBox.setScheme(MissionBox.MBX_LED2_BTN_GREEN, FOREVER + ";0,1000,1000,1000");

                MissionBox.setScheme(MissionBox.MBX_LED_PROGRESS1_RED, FOREVER + ";350,0,0,350,0,350,0,3000");
                MissionBox.setScheme(MissionBox.MBX_LED_PROGRESS1_YELLOW, FOREVER + ";0,350,350,0,0,350,0,3000");
                MissionBox.setScheme(MissionBox.MBX_LED_PROGRESS1_GREEN, FOREVER + ";0,350,0,350,350,0,0,3000");
                MissionBox.setScheme(MissionBox.MBX_LED_PROGRESS2_RED, FOREVER + ";350,0,0,350,0,350,0,3000");
                MissionBox.setScheme(MissionBox.MBX_LED_PROGRESS2_YELLOW, FOREVER + ";0,350,350,0,0,350,0,3000");
                MissionBox.setScheme(MissionBox.MBX_LED_PROGRESS2_GREEN, FOREVER + ";0,350,0,350,350,0,0,3000");

                lastAnnouncedMinute = -1;
                coldcountdownrunning.set(false);
                hotcountdownrunning.set(false);

            } else if (messageEvent.getGameState() == Farcry1AssaultThread.GAME_OUTCOME_FLAG_DEFENDED) {
                /***
                 *      _____ _             ____        __                _          _
                 *     |  ___| | __ _  __ _|  _ \  ___ / _| ___ _ __   __| | ___  __| |
                 *     | |_  | |/ _` |/ _` | | | |/ _ \ |_ / _ \ '_ \ / _` |/ _ \/ _` |
                 *     |  _| | | (_| | (_| | |_| |  __/  _|  __/ | | | (_| |  __/ (_| |
                 *     |_|   |_|\__,_|\__, |____/ \___|_|  \___|_| |_|\__,_|\___|\__,_|
                 *                    |___/
                 */
                logger.debug("GAME_OUTCOME_FLAG_DEFENDED");

                MissionBox.off(MissionBox.MBX_LED1_BTN_RED);
                MissionBox.off(MissionBox.MBX_LED1_BTN_GREEN);
                MissionBox.off(MissionBox.MBX_LED2_BTN_RED);
                MissionBox.off(MissionBox.MBX_LED2_BTN_GREEN);


                MissionBox.off(MissionBox.MBX_LED_PROGRESS1_RED);
                MissionBox.off(MissionBox.MBX_LED_PROGRESS1_YELLOW);
                MissionBox.off(MissionBox.MBX_LED_PROGRESS1_GREEN);
                MissionBox.off(MissionBox.MBX_LED_PROGRESS2_RED);
                MissionBox.off(MissionBox.MBX_LED_PROGRESS2_YELLOW);
                MissionBox.off(MissionBox.MBX_LED_PROGRESS2_GREEN);

                MissionBox.setScheme(MissionBox.MBX_LED_PROGRESS1_GREEN, FOREVER + ";500,500");
                MissionBox.setScheme(MissionBox.MBX_LED_PROGRESS2_GREEN, FOREVER + ";500,500");
                MissionBox.setScheme(MissionBox.MBX_LED1_BTN_GREEN, FOREVER + ";500,500");
                MissionBox.setScheme(MissionBox.MBX_LED2_BTN_GREEN, FOREVER + ";500,500");

                // Einmal langer Heulton zum Ende, heisst verloren
//                MissionBox.setScheme(MissionBox.MBX_SHUTDOWN_SIREN, "1;3000,0");
                MissionBox.setScheme(MissionBox.MBX_AIRSIREN, "1;%d,0", MissionBox.getIntConfig(MissionBox.MBX_STARTGAME_SIRENTIME));

            } else if (messageEvent.getGameState() == Farcry1AssaultThread.GAME_OUTCOME_FLAG_TAKEN) {
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

                MissionBox.off(MissionBox.MBX_LED_PROGRESS1_RED);
                MissionBox.off(MissionBox.MBX_LED_PROGRESS1_YELLOW);
                MissionBox.off(MissionBox.MBX_LED_PROGRESS1_GREEN);
                MissionBox.off(MissionBox.MBX_LED_PROGRESS2_RED);
                MissionBox.off(MissionBox.MBX_LED_PROGRESS2_YELLOW);
                MissionBox.off(MissionBox.MBX_LED_PROGRESS2_GREEN);

                MissionBox.setScheme(MissionBox.MBX_LED_PROGRESS1_RED, FOREVER + ";500,500");
                MissionBox.setScheme(MissionBox.MBX_LED_PROGRESS2_RED, FOREVER + ";500,500");
                MissionBox.setScheme(MissionBox.MBX_LED1_BTN_RED, FOREVER + ";500,500");
                MissionBox.setScheme(MissionBox.MBX_LED2_BTN_RED, FOREVER + ";500,500");

//                MissionBox.setScheme(MissionBox.MBX_SIREN1, "1;3000,0");
                MissionBox.setScheme(MissionBox.MBX_AIRSIREN, "1;%d,0", MissionBox.getIntConfig(MissionBox.MBX_STARTGAME_SIRENTIME));

            } else if (messageEvent.getGameState() == Farcry1AssaultThread.GAME_FLAG_ACTIVE) {
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

                MissionBox.off(MissionBox.MBX_LED_PROGRESS1_RED);
                MissionBox.off(MissionBox.MBX_LED_PROGRESS1_YELLOW);
                MissionBox.off(MissionBox.MBX_LED_PROGRESS1_GREEN);
                MissionBox.off(MissionBox.MBX_LED_PROGRESS2_RED);
                MissionBox.off(MissionBox.MBX_LED_PROGRESS2_YELLOW);
                MissionBox.off(MissionBox.MBX_LED_PROGRESS2_GREEN);

                // the starting siren
                MissionBox.setScheme(MissionBox.MBX_AIRSIREN, "1;%d,0", MissionBox.getIntConfig(MissionBox.MBX_STARTGAME_SIRENTIME));
                gameJustStarted = true;
            }
        };


        farcryAssaultThread = new Farcry1AssaultThread(messageEvent -> {
        }, gameTimeListener, gameModeListener, Integer.parseInt(MissionBox.getConfig(MissionBox.FCY_GAMETIME)), Integer.parseInt(MissionBox.getConfig(MissionBox.FCY_TIME2CAPTURE)), Integer.parseInt(MissionBox.getConfig(MissionBox.FCY_RESPAWN_INTERVAL)));


        /***
         *      ____  _         ____          _    ____ ____ ___ ___
         *     | __ )| |_ _ __ |  _ \ ___  __| |  / ___|  _ \_ _/ _ \
         *     |  _ \| __| '_ \| |_) / _ \/ _` | | |  _| |_) | | | | |
         *     | |_) | |_| | | |  _ <  __/ (_| | | |_| |  __/| | |_| |
         *     |____/ \__|_| |_|_| \_\___|\__,_|  \____|_|  |___\___/
         *
         */
        MissionBox.getBtnRed().addListener((GpioPinListenerDigital) event -> {
//            logger.debug(event);
            MissionBox.getFrmTest().setButtonTestLabel("red", event.getState() == PinState.LOW); // for debugging
            if (event.getState() == PinState.LOW) {
                logger.debug("GPIO RedButton down");
                farcryAssaultThread.setFlagHot(true);
            }
        });

        /***
         *      ____  _         ____          _   ____          _
         *     | __ )| |_ _ __ |  _ \ ___  __| | / ___|_      _(_)_ __   __ _
         *     |  _ \| __| '_ \| |_) / _ \/ _` | \___ \ \ /\ / / | '_ \ / _` |
         *     | |_) | |_| | | |  _ <  __/ (_| |  ___) \ V  V /| | | | | (_| |
         *     |____/ \__|_| |_|_| \_\___|\__,_| |____/ \_/\_/ |_|_| |_|\__, |
         *                                                              |___/
         */
        MissionBox.getBtnRed().addListener(e -> {
            farcryAssaultThread.setFlagHot(true);
        });

        /***
         *      ____  _          ____                        ____ ____ ___ ___
         *     | __ )| |_ _ __  / ___|_ __ ___  ___ _ __    / ___|  _ \_ _/ _ \
         *     |  _ \| __| '_ \| |  _| '__/ _ \/ _ \ '_ \  | |  _| |_) | | | | |
         *     | |_) | |_| | | | |_| | | |  __/  __/ | | | | |_| |  __/| | |_| |
         *     |____/ \__|_| |_|\____|_|  \___|\___|_| |_|  \____|_|  |___\___/
         *
         */
        MissionBox.getBtnGreen().addListener((GpioPinListenerDigital) event -> {
            // wenn die taste heruntergedrückt wird, ist der PinState LOW
            logger.debug(ToStringBuilder.reflectionToString(event.getState()));
            MissionBox.getFrmTest().setButtonTestLabel("green", event.getState() == PinState.LOW); // for debugging
            if (event.getState() == PinState.LOW) {
                logger.debug("GPIO GreenButton down");

                // If both buttons are pressed, the red one wins.
                if (MissionBox.getBtnRed().isLow())
                    return;

                farcryAssaultThread.setFlagHot(false);
            }
        });

        /***
         *      ____  _          ____                       ____          _
         *     | __ )| |_ _ __  / ___|_ __ ___  ___ _ __   / ___|_      _(_)_ __   __ _
         *     |  _ \| __| '_ \| |  _| '__/ _ \/ _ \ '_ \  \___ \ \ /\ / / | '_ \ / _` |
         *     | |_) | |_| | | | |_| | | |  __/  __/ | | |  ___) \ V  V /| | | | | (_| |
         *     |____/ \__|_| |_|\____|_|  \___|\___|_| |_| |____/ \_/\_/ |_|_| |_|\__, |
         *                                                                        |___/
         */
        MissionBox.getBtnGreen().addListener(e -> {
            logger.debug("GreenButton clicked");
            farcryAssaultThread.setFlagHot(false);
        });

        /***
         *      ____  _         ____  _             _   ____  _                 ____ ____ ___ ___
         *     | __ )| |_ _ __ / ___|| |_ __ _ _ __| |_/ ___|| |_ ___  _ __    / ___|  _ \_ _/ _ \
         *     |  _ \| __| '_ \\___ \| __/ _` | '__| __\___ \| __/ _ \| '_ \  | |  _| |_) | | | | |
         *     | |_) | |_| | | |___) | || (_| | |  | |_ ___) | || (_) | |_) | | |_| |  __/| | |_| |
         *     |____/ \__|_| |_|____/ \__\__,_|_|   \__|____/ \__\___/| .__/   \____|_|  |___\___/
         *                                                            |_|
         */
        MissionBox.getBtnGameStartStop().addListener((GpioPinListenerDigital) event -> {
            logger.debug(ToStringBuilder.reflectionToString(event.getState()));
            MissionBox.getFrmTest().setButtonTestLabel("start", event.getState() == PinState.LOW); // for debugging
            if (!MissionBox.isGameStartable()) return;
            if (farcryAssaultThread.isPausing()) return;
            if (event.getState() == PinState.LOW) {
                logger.debug("GPIO Start/Stop down");
                if (farcryAssaultThread.getGameState() == Farcry1AssaultThread.GAME_PRE_GAME) {
                    farcryAssaultThread.startGame();
                } else {
                    farcryAssaultThread.prepareGame();
                }
            }
        });

        /***
         *      ____  _         ____  _             _   ____  _                ____          _
         *     | __ )| |_ _ __ / ___|| |_ __ _ _ __| |_/ ___|| |_ ___  _ __   / ___|_      _(_)_ __   __ _
         *     |  _ \| __| '_ \\___ \| __/ _` | '__| __\___ \| __/ _ \| '_ \  \___ \ \ /\ / / | '_ \ / _` |
         *     | |_) | |_| | | |___) | || (_| | |  | |_ ___) | || (_) | |_) |  ___) \ V  V /| | | | | (_| |
         *     |____/ \__|_| |_|____/ \__\__,_|_|   \__|____/ \__\___/| .__/  |____/ \_/\_/ |_|_| |_|\__, |
         *                                                            |_|                            |___/
         */
        MissionBox.getBtnGameStartStop().addListener(e -> {
            if (!MissionBox.isGameStartable()) return;
            if (farcryAssaultThread.isPausing()) return;
            logger.debug("btnGameStartStop");
            if (farcryAssaultThread.getGameState() == Farcry1AssaultThread.GAME_PRE_GAME) {
                farcryAssaultThread.startGame();
            } else {
                farcryAssaultThread.prepareGame();
            }
        });

        /***
         *      ____  _         ____                         ____ ____ ___ ___
         *     | __ )| |_ _ __ |  _ \ __ _ _   _ ___  ___   / ___|  _ \_ _/ _ \
         *     |  _ \| __| '_ \| |_) / _` | | | / __|/ _ \ | |  _| |_) | | | | |
         *     | |_) | |_| | | |  __/ (_| | |_| \__ \  __/ | |_| |  __/| | |_| |
         *     |____/ \__|_| |_|_|   \__,_|\__,_|___/\___|  \____|_|  |___\___/
         *
         */
        MissionBox.getBtnPAUSE().addListener((GpioPinListenerDigital) event -> {
            logger.debug(ToStringBuilder.reflectionToString(event.getState()));
            MissionBox.getFrmTest().setButtonTestLabel("pause", event.getState() == PinState.LOW); // for debugging
            if (event.getState() == PinState.LOW) {
                logger.debug("GPIO BtnPause down");
                farcryAssaultThread.togglePause();
            }
        });

        /***
         *      ____  _         ____                        ____          _
         *     | __ )| |_ _ __ |  _ \ __ _ _   _ ___  ___  / ___|_      _(_)_ __   __ _
         *     |  _ \| __| '_ \| |_) / _` | | | / __|/ _ \ \___ \ \ /\ / / | '_ \ / _` |
         *     | |_) | |_| | | |  __/ (_| | |_| \__ \  __/  ___) \ V  V /| | | | | (_| |
         *     |____/ \__|_| |_|_|   \__,_|\__,_|___/\___| |____/ \_/\_/ |_|_| |_|\__, |
         *                                                                        |___/
         */
        MissionBox.getBtnPAUSE().addListener(e -> {
            logger.debug("btnPause - on Screen");
            farcryAssaultThread.togglePause();
        });

    } // constructor


    @Override
    public void stopGame() {

    }

    @Override
    public void runGame() {
        farcryAssaultThread.run();
    }


    public void prepareGame() {
        farcryAssaultThread.prepareGame();
    }

    public void setRevertEvent(Farcry1GameEvent revertEvent) {
        farcryAssaultThread.setRevertEvent(revertEvent);
    }
}
