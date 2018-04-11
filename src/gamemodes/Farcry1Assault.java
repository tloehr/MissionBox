package gamemodes;

import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import interfaces.FC1DetailsMessageEvent;
import interfaces.MessageListener;
import main.Main;
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
    private int lastAnnouncedSecond = -1;
    private boolean lastMinuteAnnounced = false;


    // das mach ich nur, weil ich diese beiden Flags als finals brauche.
    private final AtomicBoolean coldcountdownrunning, hotcountdownrunning, overtime;

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

        logger.setLevel(Main.getLogLevel());

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
        overtime = new AtomicBoolean(false);

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
            if (Long.parseLong(Main.getConfig(Main.FCY_RESPAWN_INTERVAL)) > 0l) {
                if (messageEvent.getGameState() == Farcry1AssaultThread.GAME_FLAG_HOT || messageEvent.getGameState() == Farcry1AssaultThread.GAME_FLAG_COLD) {

                    FC1DetailsMessageEvent event = (FC1DetailsMessageEvent) messageEvent;
                    String respawnTimer = Tools.formatLongTime(event.getLastrespawn() + event.getRespawninterval() - event.getGametimer(), "mm:ss");
                    Main.setRespawnTimer(respawnTimer);
//                    logger.debug(event.getLastrespawn() + ", " + event.getRespawninterval() + " , " + event.getGametimer());
                    if (event.getLastrespawn() + event.getRespawninterval() <= event.getGametimer()) {
                        Main.setScheme(Main.MBX_RESPAWN_SIREN, "1;%d,0", Main.getIntConfig(Main.MBX_RESPAWN_SIRENTIME));
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
                        Main.setScheme(Main.MBX_SIREN1, "10;500,500"); // eine Sekunde mehr. Dann gibts nicht so eine Lücke beim Ende
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
                            Main.setScheme(Main.MBX_SHUTDOWN_SIREN, "10;500,500"); // eine Sekunde mehr. Dann gibts nicht so eine Lücke beim Ende
                        }
                    }
                }
            }


            if (messageEvent.getGameState() == Farcry1AssaultThread.GAME_PRE_GAME) {

                Main.setTimerMessage("--");
                Main.setMessage(Tools.h1(Tools.xx("fc1assault.gamestate." + Farcry1AssaultThread.GAMSTATS[messageEvent.getGameState()])));
                Main.setRespawnTimer("--");
            } else if (messageEvent.getGameState() == Farcry1AssaultThread.GAME_FLAG_HOT) {
                Main.setTimerMessage(((FC1DetailsMessageEvent) messageEvent).toHTML());
                long remain = farcryAssaultThread.getRemaining();

                String message = Tools.h1(Tools.xx("fc1assault.gamestate." + Farcry1AssaultThread.GAMSTATS[messageEvent.getGameState()]) + " " + Tools.formatLongTime(remain, "mm:ss"));

                // das muss ich hier machen, weil der gametimer nur dann noch richtig ist, solange das spiel
                // läuft. Wenn ich den nach Ende setzen, sind dann noch ein paar Millis mehr auf der Uhr.
                // und dann klappen die Abfragen nicht mehr.
                overtime.set(((FC1DetailsMessageEvent) messageEvent).isOvertime());

                if (overtime.get()) {
                    message += "<h2> OVERTIME: " + Tools.formatLongTime(((FC1DetailsMessageEvent) messageEvent).getOvertime(), "mm:ss") + "</h2>";
                }


                Main.setMessage(message);
                Main.setProgress(((FC1DetailsMessageEvent) messageEvent).getTimeWhenTheFlagWasActivated(), ((FC1DetailsMessageEvent) messageEvent).getGametimer(), ((FC1DetailsMessageEvent) messageEvent).getTimeWhenTheFlagWasActivated() + ((FC1DetailsMessageEvent) messageEvent).getCapturetime());

            } else if (messageEvent.getGameState() == Farcry1AssaultThread.GAME_FLAG_COLD) {
                Main.setTimerMessage(((FC1DetailsMessageEvent) messageEvent).toHTML());
                long remain = farcryAssaultThread.getRemaining();
                DateTime remainingTime = new DateTime(remain, DateTimeZone.UTC);

                int minutes = remainingTime.getMinuteOfHour();
                int seconds = remainingTime.getSecondOfMinute();

                // mehr als 1 Minute
                if (lastAnnouncedMinute != minutes) {
                    lastAnnouncedMinute = minutes;
                    logger.debug("time announcer: " + minutes + ":" + seconds);
                    if (minutes > 0) {
                        String scheme = "";
                        for (int m = 1; m < minutes; m++) {
                            scheme += "250,250,";
                        }

                        scheme += "250,10000";

                        Main.setScheme(Main.MBX_LED_PROGRESS1_GREEN, "10000;" + scheme);
                        Main.setScheme(Main.MBX_LED_PROGRESS2_GREEN, "10000;" + scheme);
                    } else {
                        Main.off(Main.MBX_LED_PROGRESS1_GREEN);
                        Main.off(Main.MBX_LED_PROGRESS2_GREEN);
                    }
                }

                if (!lastMinuteAnnounced && minutes == 1 && seconds == 0) {
                    lastMinuteAnnounced = true;
                    Main.setScheme(Main.MBX_SHUTDOWN_SIREN, "1;1000,0");
                }

                // weniger als 1 minute
                if (minutes < 1) {
                    if (lastAnnouncedSecond != 1) { // muss ja nur einmal aufgerufen werden.
                        lastAnnouncedSecond = 1;
                        logger.debug("blinken in den letzten 60 Sekunden");

                        Main.setScheme(Main.MBX_LED_PROGRESS1_GREEN, "10000;250,500");
                        Main.setScheme(Main.MBX_LED_PROGRESS2_GREEN, "10000;250,500");

                    }
                }

                String message = Tools.h1(Tools.xx("fc1assault.gamestate." + Farcry1AssaultThread.GAMSTATS[messageEvent.getGameState()]) + " " + Tools.formatLongTime(remain, "mm:ss"));
                Main.setMessage(message);
            } else if (messageEvent.getGameState() == Farcry1AssaultThread.GAME_PAUSING) {
                Main.setTimerMessage(((FC1DetailsMessageEvent) messageEvent).toHTML());
                long pausingsince = System.currentTimeMillis() - ((FC1DetailsMessageEvent) messageEvent).getPausingSince();
                String message = Tools.h1(Tools.xx("fc1assault.gamestate." + Farcry1AssaultThread.GAMSTATS[messageEvent.getGameState()]) + " " + Tools.formatLongTime(pausingsince, "mm:ss"));
                Main.setMessage(message);
            } else if (messageEvent.getGameState() == Farcry1AssaultThread.GAME_GOING_TO_RESUME) {
                Main.setTimerMessage(((FC1DetailsMessageEvent) messageEvent).toHTML());
                long resumein = ((FC1DetailsMessageEvent) messageEvent).getResumingSince() + ((FC1DetailsMessageEvent) messageEvent).getResumeinterval() - System.currentTimeMillis() + 1000; // 1 sekunde drauf, weg der Anzeige
                String message = Tools.h1(Tools.xx("fc1assault.gamestate." + Farcry1AssaultThread.GAMSTATS[messageEvent.getGameState()]) + " " + Tools.formatLongTime(resumein, "mm:ss"));
                Main.setMessage(message);
            } else {
                Main.setTimerMessage("Don't know");
                Main.setRespawnTimer("--");
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
                Main.setScheme(Main.MBX_LED1_BTN_GREEN, FOREVER + ";250,250");
                Main.off(Main.MBX_LED1_BTN_RED);
                Main.setScheme(Main.MBX_LED2_BTN_GREEN, FOREVER + ";250,250");
                Main.off(Main.MBX_LED2_BTN_RED);

                // anders rum (bei cold) brauchen wir das nicht, weil diese sirene über das Percentage Interface abgeschaltet wird.
                if (coldcountdownrunning.get()) Main.off(Main.MBX_SHUTDOWN_SIREN);

                lastAnnouncedMinute = -1;
                lastAnnouncedSecond = -1;
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

                Main.setProgress(new BigDecimal(-1));

                Main.off(Main.MBX_LED1_BTN_GREEN);
                Main.off(Main.MBX_LED2_BTN_GREEN);

                Main.off(Main.MBX_LED_PROGRESS1_RED);
                Main.off(Main.MBX_LED_PROGRESS1_YELLOW);
                Main.off(Main.MBX_LED_PROGRESS1_GREEN);

                Main.off(Main.MBX_LED_PROGRESS2_RED);
                Main.off(Main.MBX_LED_PROGRESS2_YELLOW);
                Main.off(Main.MBX_LED_PROGRESS2_GREEN);

                Main.setScheme(Main.MBX_LED1_BTN_RED, FOREVER + ";250,250");
                Main.setScheme(Main.MBX_LED2_BTN_RED, FOREVER + ";250,250");


                // LED Anzeige, welche die Langeweile der Verteidiger ausdrückt.


                // damit beim Anfang nicht direkt die Shutdown Sirene ertönt
                // UND damit bei einem Overtime die End-Sirene und die Shutdown-Sirene nicht kollidieren
                if (!gameJustStarted && ((FC1DetailsMessageEvent) messageEvent).getOvertime() < 0) {
                    Main.setScheme(Main.MBX_SHUTDOWN_SIREN, "1;2000,0");
                }

                gameJustStarted = false;
                lastAnnouncedMinute = -1;
                lastAnnouncedSecond = -1;
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

                Main.enableSettings(true);
                Main.getFrmTest().getBtnClearEvent().setEnabled(false);

                Main.setProgress(BigDecimal.ONE.negate()); // stop progess. -1 beendet alles.


                Main.off(Main.MBX_SHUTDOWN_SIREN);
                Main.off(Main.MBX_AIRSIREN);
                Main.off(Main.MBX_SIREN1);
                Main.off(Main.MBX_RESPAWN_SIREN);

                Main.off(Main.MBX_RESPAWN_SIREN);

                Main.setScheme(Main.MBX_LED1_BTN_RED, FOREVER + ";1000,1000");
                Main.setScheme(Main.MBX_LED1_BTN_GREEN, FOREVER + ";0,1000,1000,1000");

                Main.setScheme(Main.MBX_LED2_BTN_RED, FOREVER + ";1000,1000");
                Main.setScheme(Main.MBX_LED2_BTN_GREEN, FOREVER + ";0,1000,1000,1000");

                Main.setScheme(Main.MBX_LED_PROGRESS1_RED, FOREVER + ";350,0,0,350,0,350,0,3000");
                Main.setScheme(Main.MBX_LED_PROGRESS1_YELLOW, FOREVER + ";0,350,350,0,0,350,0,3000");
                Main.setScheme(Main.MBX_LED_PROGRESS1_GREEN, FOREVER + ";0,350,0,350,350,0,0,3000");
                Main.setScheme(Main.MBX_LED_PROGRESS2_RED, FOREVER + ";350,0,0,350,0,350,0,3000");
                Main.setScheme(Main.MBX_LED_PROGRESS2_YELLOW, FOREVER + ";0,350,350,0,0,350,0,3000");
                Main.setScheme(Main.MBX_LED_PROGRESS2_GREEN, FOREVER + ";0,350,0,350,350,0,0,3000");

                lastMinuteAnnounced = false;
                lastAnnouncedMinute = -1;
                lastAnnouncedSecond = -1;
                coldcountdownrunning.set(false);
                hotcountdownrunning.set(false);
                overtime.set(false);
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

                Main.off(Main.MBX_SHUTDOWN_SIREN);

                Main.off(Main.MBX_LED1_BTN_RED);
                Main.off(Main.MBX_LED1_BTN_GREEN);
                Main.off(Main.MBX_LED2_BTN_RED);
                Main.off(Main.MBX_LED2_BTN_GREEN);

                Main.off(Main.MBX_LED_PROGRESS1_RED);
                Main.off(Main.MBX_LED_PROGRESS1_YELLOW);
                Main.off(Main.MBX_LED_PROGRESS1_GREEN);
                Main.off(Main.MBX_LED_PROGRESS2_RED);
                Main.off(Main.MBX_LED_PROGRESS2_YELLOW);
                Main.off(Main.MBX_LED_PROGRESS2_GREEN);

                Main.setScheme(Main.MBX_LED_PROGRESS1_GREEN, FOREVER + ";500,500");
                Main.setScheme(Main.MBX_LED_PROGRESS2_GREEN, FOREVER + ";500,500");
                Main.setScheme(Main.MBX_LED1_BTN_GREEN, FOREVER + ";500,500");
                Main.setScheme(Main.MBX_LED2_BTN_GREEN, FOREVER + ";500,500");

                // Einmal langer Heulton zum Ende, heisst verloren
//                MissionBox.setScheme(MissionBox.MBX_SHUTDOWN_SIREN, "1;3000,0");
                Main.setScheme(Main.MBX_AIRSIREN, "1;%d,0", Main.getIntConfig(Main.MBX_STARTGAME_SIRENTIME));

                String message = Tools.h1(Tools.xx("fc1assault.gamestate." + Farcry1AssaultThread.GAMSTATS[messageEvent.getGameState()]));
                if (overtime.get()) {
                    message += "<h2>SUDDEN DEATH (Overtime)</h2>";
                }

                Main.setMessage(message);
                Main.setRespawnTimer("--");

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

                Main.off(Main.MBX_LED1_BTN_RED);
                Main.off(Main.MBX_LED1_BTN_GREEN);
                Main.off(Main.MBX_LED2_BTN_RED);
                Main.off(Main.MBX_LED2_BTN_GREEN);

                Main.off(Main.MBX_LED_PROGRESS1_RED);
                Main.off(Main.MBX_LED_PROGRESS1_YELLOW);
                Main.off(Main.MBX_LED_PROGRESS1_GREEN);
                Main.off(Main.MBX_LED_PROGRESS2_RED);
                Main.off(Main.MBX_LED_PROGRESS2_YELLOW);
                Main.off(Main.MBX_LED_PROGRESS2_GREEN);

                Main.setScheme(Main.MBX_LED_PROGRESS1_RED, FOREVER + ";500,500");
                Main.setScheme(Main.MBX_LED_PROGRESS2_RED, FOREVER + ";500,500");
                Main.setScheme(Main.MBX_LED1_BTN_RED, FOREVER + ";500,500");
                Main.setScheme(Main.MBX_LED2_BTN_RED, FOREVER + ";500,500");

//                MissionBox.setScheme(MissionBox.MBX_SIREN1, "1;3000,0");
                Main.setScheme(Main.MBX_AIRSIREN, "1;%d,0", Main.getIntConfig(Main.MBX_STARTGAME_SIRENTIME));

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
                Main.enableSettings(false);

                Main.off(Main.MBX_LED_PROGRESS1_RED);
                Main.off(Main.MBX_LED_PROGRESS1_YELLOW);
                Main.off(Main.MBX_LED_PROGRESS1_GREEN);
                Main.off(Main.MBX_LED_PROGRESS2_RED);
                Main.off(Main.MBX_LED_PROGRESS2_YELLOW);
                Main.off(Main.MBX_LED_PROGRESS2_GREEN);

                // the starting siren
                Main.setScheme(Main.MBX_AIRSIREN, "1;%d,0", Main.getIntConfig(Main.MBX_STARTGAME_SIRENTIME));
                gameJustStarted = true;
            }
        };


        farcryAssaultThread = new Farcry1AssaultThread(messageEvent -> {
        }, gameTimeListener, gameModeListener, Integer.parseInt(Main.getConfig(Main.FCY_GAMETIME)), Integer.parseInt(Main.getConfig(Main.FCY_TIME2CAPTURE)), Integer.parseInt(Main.getConfig(Main.FCY_RESPAWN_INTERVAL)));


        /***
         *      ____  _         ____          _    ____ ____ ___ ___
         *     | __ )| |_ _ __ |  _ \ ___  __| |  / ___|  _ \_ _/ _ \
         *     |  _ \| __| '_ \| |_) / _ \/ _` | | |  _| |_) | | | | |
         *     | |_) | |_| | | |  _ <  __/ (_| | | |_| |  __/| | |_| |
         *     |____/ \__|_| |_|_| \_\___|\__,_|  \____|_|  |___\___/
         *
         */
        Main.getBtnRed().addListener((GpioPinListenerDigital) event -> {
//            logger.debug(event);
            Main.getFrmTest().setButtonTestLabel("red", event.getState() == PinState.LOW); // for debugging
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
        Main.getBtnRed().addListener(e -> {
            logger.debug("SWING RedButton clicked");
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
        Main.getBtnGreen().addListener((GpioPinListenerDigital) event -> {
            // wenn die taste heruntergedrückt wird, ist der PinState LOW
            logger.debug(ToStringBuilder.reflectionToString(event.getState()));
            Main.getFrmTest().setButtonTestLabel("green", event.getState() == PinState.LOW); // for debugging
            if (event.getState() == PinState.LOW) {
                logger.debug("GPIO GreenButton down");

                // If both buttons are pressed, the red one wins.
                if (Main.getBtnRed().isLow())
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
        Main.getBtnGreen().addListener(e -> {
            logger.debug("SWING GreenButton clicked");
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
        Main.getBtnGameStartStop().addListener((GpioPinListenerDigital) event -> {
            logger.debug(ToStringBuilder.reflectionToString(event.getState()));
            Main.getFrmTest().setButtonTestLabel("start", event.getState() == PinState.LOW); // for debugging
            if (!Main.isGameStartable()) return;
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
        Main.getBtnGameStartStop().addListener(e -> {
            if (!Main.isGameStartable()) return;
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
        Main.getBtnPAUSE().addListener((GpioPinListenerDigital) event -> {
            logger.debug(ToStringBuilder.reflectionToString(event.getState()));
            Main.getFrmTest().setButtonTestLabel("pause", event.getState() == PinState.LOW); // for debugging
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
        Main.getBtnPAUSE().addListener(e -> {
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
