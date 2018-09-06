package de.flashheart.missionbox.gamemodes;

import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import de.flashheart.missionbox.Main;
import de.flashheart.missionbox.events.FC1GameEvent;
import de.flashheart.missionbox.events.MessageListener;
import de.flashheart.missionbox.misc.Configs;
import de.flashheart.missionbox.misc.HasLogger;
import de.flashheart.missionbox.misc.Tools;
import de.flashheart.missionbox.rlggames.GameEvent;
import de.flashheart.missionbox.statistics.Statistics;
import org.apache.commons.lang3.builder.ToStringBuilder;
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
public class Farcry1Assault implements GameMode, HasLogger {
    private boolean gameJustStarted, gameJustResumed;
    private Farcry1AssaultThread farcryAssaultThread;
    //    private String FOREVER = Integer.toString(Integer.MAX_VALUE);
    //    private String lastAnnoucement = "";
    private int lastAnnouncedMinute = -1;
    private int lastAnnouncedSecond = -1;
    private boolean lastMinuteAnnounced = false;
    private long lastStatsSent, min_stat_sent_time;
    private Statistics statistics;

    // das mach ich nur, weil ich diese beiden Flags als finals brauche.
    private final AtomicBoolean coldcountdownrunning, hotcountdownrunning, overtime, resumecountdownrunning;

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
    public Farcry1Assault() {

        getLogger().info("\n" +
                "      ____  _             _   _               _____           ____                 _                        _ _   \n" +
                "     / ___|| |_ __ _ _ __| |_(_)_ __   __ _  |  ___|_ _ _ __ / ___|_ __ _   _     / \\   ___ ___  __ _ _   _| | |_ \n" +
                "     \\___ \\| __/ _` | '__| __| | '_ \\ / _` | | |_ / _` | '__| |   | '__| | | |   / _ \\ / __/ __|/ _` | | | | | __|\n" +
                "      ___) | || (_| | |  | |_| | | | | (_| | |  _| (_| | |  | |___| |  | |_| |  / ___ \\\\__ \\__ \\ (_| | |_| | | |_ \n" +
                "     |____/ \\__\\__,_|_|   \\__|_|_| |_|\\__, | |_|  \\__,_|_|   \\____|_|   \\__, | /_/   \\_\\___/___/\\__,_|\\__,_|_|\\__|\n" +
                "                                      |___/                             |___/                                     \n" +
                "");

        min_stat_sent_time = Long.parseLong(Main.getConfigs().get(Configs.MIN_STAT_SEND_TIME));
        statistics = new Statistics();
        coldcountdownrunning = new AtomicBoolean(false);
        hotcountdownrunning = new AtomicBoolean(false);
        resumecountdownrunning = new AtomicBoolean(false);
        gameJustResumed = false;
        overtime = new AtomicBoolean(false);

        /***
         *                                _____ _                     _     _     _
         *       __ _  __ _ _ __ ___   __|_   _(_)_ __ ___   ___ _ __| |   (_)___| |_
         *      / _` |/ _` | '_ ` _ \ / _ \| | | | '_ ` _ \ / _ \ '__| |   | / __| __|
         *     | (_| | (_| | | | | | |  __/| | | | | | | | |  __/ |  | |___| \__ \ |_
         *      \__, |\__,_|_| |_| |_|\___||_| |_|_| |_| |_|\___|_|  |_____|_|___/\__|
         *      |___/
         */
        MessageListener gameTimeListener = messageEvent -> {

            long now = System.currentTimeMillis();

            // Statistiken, wenn gewünscht

            if (min_stat_sent_time > 0 && farcryAssaultThread.isGameRunning()) {
                statistics.updateTimers(messageEvent);
                if (now - lastStatsSent > min_stat_sent_time) {
                    statistics.sendStats();
                    lastStatsSent = now;
                }
            }


            /***
             *      ____                                        ____  _                   _
             *     |  _ \ ___  ___ _ __   __ ___      ___ __   / ___|(_) __ _ _ __   __ _| |
             *     | |_) / _ \/ __| '_ \ / _` \ \ /\ / / '_ \  \___ \| |/ _` | '_ \ / _` | |
             *     |  _ <  __/\__ \ |_) | (_| |\ V  V /| | | |  ___) | | (_| | | | | (_| | |
             *     |_| \_\___||___/ .__/ \__,_| \_/\_/ |_| |_| |____/|_|\__, |_| |_|\__,_|_|
             *                    |_|                                   |___/
             */
            if (Long.parseLong(Main.getConfigs().get(Configs.FCY_RESPAWN_INTERVAL)) > 0l) {
                if (messageEvent.getEvent() == GameEvent.FUSED|| messageEvent.getEvent() == GameEvent.DEFUSED) {

                    FC1GameEvent event = (FC1GameEvent) messageEvent;
                    String respawnTimer = Tools.formatLongTime(event.getLastrespawn() + event.getRespawninterval() - event.getGametime(), "HH:mm:ss");
                    Main.setRespawnTimer(respawnTimer);
//                    getLogger().debug(event.getLastrespawn() + ", " + event.getRespawninterval() + " , " + event.getGametimer());
                    if (event.getLastrespawn() + event.getRespawninterval() <= event.getGametime()) {
                        Main.getPinHandler().setScheme(Main.NAME_RESPAWN_SIREN, String.format("1:on,%s;off,∞", Main.getConfigs().get(Configs.MBX_RESPAWN_SIRENTIME)));
                        getLogger().info("\n" +
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
            if (messageEvent.getEvent() == GameEvent.FUSED) {
                /***
                 *      _   _  ___ _____
                 *     | | | |/ _ \_   _|
                 *     | |_| | | | || |
                 *     |  _  | |_| || |
                 *     |_| |_|\___/ |_|
                 *
                 */
                FC1GameEvent event = (FC1GameEvent) messageEvent;

                if (!hotcountdownrunning.get()) {
                    Interval remaining = new Interval(event.getGametime(), event.getTimeWhenTheFlagWasActivated() + event.getCapturetime());
                    if (Seconds.secondsIn(remaining).getSeconds() < 10) { // die letzten 10 Sekunden laufen
                        hotcountdownrunning.set(true);
                        Main.getPinHandler().setScheme(Main.NAME_SIREN1, "10:on,500;off,500"); // eine Sekunde mehr. Dann gibts nicht so eine Lücke beim Ende
                    }
                }
            } else if (messageEvent.getEvent() == GameEvent.DEFUSED) {
                /***
                 *       ____ ___  _     ____
                 *      / ___/ _ \| |   |  _ \
                 *     | |  | | | | |   | | | |
                 *     | |__| |_| | |___| |_| |
                 *      \____\___/|_____|____/
                 *
                 */
                FC1GameEvent event = (FC1GameEvent) messageEvent;

                if (!event.isOvertime()) { // Sonst gibts eine Exception beim Interval (gametimer > maxgametime)
                    // Spielt bei HOT keine Rolle.
                    if (!coldcountdownrunning.get()) {
                        Interval remaining = new Interval(event.getGametime(), event.getMaxgametime());
                        if (Seconds.secondsIn(remaining).getSeconds() < 10) { // die letzten 10 Sekunden laufen
                            coldcountdownrunning.set(true);
                            Main.getPinHandler().setScheme(Main.NAME_SHUTDOWN_SIREN, "10:on,500;off,500"); // eine Sekunde mehr. Dann gibts nicht so eine Lücke beim Ende
                        }
                    }
                }
            }

            if (messageEvent.getEvent() == GameEvent.PREGAME) {
                Main.setTimerMessage("--");
                Main.getFrmTest().setMessage(Tools.h1(Tools.xx("fc1assault.gamestate." + messageEvent.getEvent())));
                Main.setRespawnTimer("--");
            } else if (messageEvent.getEvent() == GameEvent.FUSED) {
                Main.setTimerMessage(((FC1GameEvent) messageEvent).toHTML());
                long remain = farcryAssaultThread.getRemaining();

                String message = Tools.h1(Tools.xx("fc1assault.gamestate." + messageEvent.getEvent()) + " " + Tools.formatLongTime(remain, "HH:mm:ss"));

                // das muss ich hier machen, weil der gametimer nur dann noch richtig ist, solange das spiel
                // läuft. Wenn ich den nach Ende setzen, sind dann noch ein paar Millis mehr auf der Uhr.
                // und dann klappen die Abfragen nicht mehr.
                overtime.set(((FC1GameEvent) messageEvent).isOvertime());

                if (overtime.get()) {
                    message += "<h2> OVERTIME: " + Tools.formatLongTime(((FC1GameEvent) messageEvent).getOvertime(), "mm:ss") + "</h2>";
//                    lastStatsSent = statistics.addEvent(new GameEvent(messageEvent, Statistics.GAME_OVERTIME));
                }


                Main.getFrmTest().setMessage(message);
                Main.setProgress(((FC1GameEvent) messageEvent).getTimeWhenTheFlagWasActivated(), ((FC1GameEvent) messageEvent).getGametime(), ((FC1GameEvent) messageEvent).getTimeWhenTheFlagWasActivated() + ((FC1GameEvent) messageEvent).getCapturetime());

            } else if (messageEvent.getEvent() == GameEvent.DEFUSED) {
                Main.setTimerMessage(((FC1GameEvent) messageEvent).toHTML());
                long remain = farcryAssaultThread.getRemaining();
                DateTime remainingTime = new DateTime(remain, DateTimeZone.UTC);


                int minutes = remainingTime.getMinuteOfHour();
                int seconds = remainingTime.getSecondOfMinute();

                // mehr als 1 Minute
                if (lastAnnouncedMinute != minutes) {
                    lastAnnouncedMinute = minutes;

                    int hours = remainingTime.getHourOfDay();
                    int tenminutes = minutes / 10;
                    int remminutes = minutes - tenminutes * 10; // restliche Minuten ausrechnen

                    getLogger().debug("time announcer: " + hours + ":" + minutes + ":" + seconds);
                    getLogger().debug("time announcer: remaining 10 minutes chunks: " + tenminutes);

                    if (minutes > 0) {
                        String scheme = "";
                        if (hours > 0) {
                            for (int h = 0; h < hours; h++) {
                                scheme += "on,1000;off,250;";
                            }
                        }

                        if (tenminutes > 0) {
                            for (int tm = 0; tm < tenminutes; tm++) {
                                scheme += "on,625;off,250;";
                            }
                        }

                        if (remminutes > 0) {
                            for (int rm = 0; rm < remminutes; rm++) {
                                scheme += "on,250;off,250;";
                            }
                        }

                        scheme += "off,2500";

                        Main.getPinHandler().setScheme(Main.NAME_LED1_PROGRESS_GREEN, "∞:" + scheme);
                        Main.getPinHandler().setScheme(Main.NAME_LED2_PROGRESS_GREEN, "∞:" + scheme);

                    } else {
                        Main.getPinHandler().off(Main.NAME_LED1_PROGRESS_GREEN);
                        Main.getPinHandler().off(Main.NAME_LED2_PROGRESS_GREEN);
                    }
                }

                if (!lastMinuteAnnounced && minutes == 1 && seconds == 0) {
                    lastMinuteAnnounced = true;
                    Main.getPinHandler().setScheme(Main.NAME_SHUTDOWN_SIREN, "1:on,1000;off,0"); //todo: prüfen
                }

                // weniger als 1 minute
                if (minutes < 1) {
                    if (lastAnnouncedSecond != 1) { // muss ja nur einmal aufgerufen werden.
                        lastAnnouncedSecond = 1;
                        getLogger().debug("blinken in den letzten 60 Sekunden");

                        Main.getPinHandler().setScheme(Main.NAME_LED1_PROGRESS_GREEN, "∞:on,250;off,250");
                        Main.getPinHandler().setScheme(Main.NAME_LED2_PROGRESS_GREEN, "∞:on,250;off,250");

                    }
                }

                String message = Tools.h1(Tools.xx("fc1assault.gamestate." + messageEvent.getEvent()) + " " + Tools.formatLongTime(remain, "HH:mm:ss"));
                Main.getFrmTest().setMessage(message);
            } else if (messageEvent.getEvent() == GameEvent.PAUSING) {
                Main.setTimerMessage(messageEvent.toHTML());
                long pausingsince = System.currentTimeMillis() - messageEvent.getPausingSince();
                String message = Tools.h1(Tools.xx("fc1assault.gamestate." + messageEvent.getEvent()) + " " + Tools.formatLongTime(pausingsince, "HH:mm:ss"));
                Main.getFrmTest().setMessage(message);
            } else if (messageEvent.getEvent() == GameEvent.GOING_TO_RESUME) {
                Main.setTimerMessage(messageEvent.toHTML());
                long resumein = messageEvent.getResumingSince() + messageEvent.getResumeinterval() - System.currentTimeMillis() + 1000; // 1 sekunde drauf, weg der Anzeige
                String message = Tools.h1(Tools.xx("fc1assault.gamestate." + messageEvent.getEvent()) + " " + Tools.formatLongTime(resumein, "HH:mm:ss"));
                Main.getFrmTest().setMessage(message);
            } else {
                Main.setTimerMessage("Don't know");
                Main.setRespawnTimer("--");
            }

        };

        MessageListener gameModeListener = messageEvent -> {
            if (messageEvent.getEvent() != GameEvent.PREGAME)
                lastStatsSent = statistics.addEvent(messageEvent);

            if (messageEvent.getEvent() == GameEvent.RESUMED) {
                gameJustResumed = true;
            } else if (messageEvent.getEvent() == GameEvent.FUSED) {
                /***
                 *      _____ _             _   _       _
                 *     |  ___| | __ _  __ _| | | | ___ | |_
                 *     | |_  | |/ _` |/ _` | |_| |/ _ \| __|
                 *     |  _| | | (_| | (_| |  _  | (_) | |_
                 *     |_|   |_|\__,_|\__, |_| |_|\___/ \__|
                 *                    |___/
                 */

                Main.getPinHandler().setScheme(Main.NAME_LED1_BTN_GREEN, "∞:on,250;off,250");
                Main.getPinHandler().off(Main.NAME_LED1_BTN_RED);
                Main.getPinHandler().setScheme(Main.NAME_LED2_BTN_GREEN, "∞:on,250;off,250");
                Main.getPinHandler().off(Main.NAME_LED2_BTN_RED);

                // anders rum (bei cold) brauchen wir das nicht, weil diese sirene über das Percentage Interface abgeschaltet wird.
                if (coldcountdownrunning.get()) Main.getPinHandler().off(Main.NAME_SHUTDOWN_SIREN);

                lastAnnouncedMinute = -1;
                lastAnnouncedSecond = -1;
                hotcountdownrunning.set(false);
                coldcountdownrunning.set(false);

                // die hauptsirene wird hier nicht aktiviert, weil sie über das PercentageInterface läuft.

            } else if (messageEvent.getEvent() == GameEvent.DEFUSED) {
                /***
                 *      _____ _              ____      _     _
                 *     |  ___| | __ _  __ _ / ___|___ | | __| |
                 *     | |_  | |/ _` |/ _` | |   / _ \| |/ _` |
                 *     |  _| | | (_| | (_| | |__| (_) | | (_| |
                 *     |_|   |_|\__,_|\__, |\____\___/|_|\__,_|
                 *                    |___/
                 */


                Main.setProgress(new BigDecimal(-1));

                Main.getPinHandler().off(Main.NAME_LED1_BTN_GREEN);
                Main.getPinHandler().off(Main.NAME_LED2_BTN_GREEN);

                Main.getPinHandler().off(Main.NAME_LED1_PROGRESS_RED);
                Main.getPinHandler().off(Main.NAME_LED1_PROGRESS_YELLOW);
                Main.getPinHandler().off(Main.NAME_LED1_PROGRESS_GREEN);

                Main.getPinHandler().off(Main.NAME_LED2_PROGRESS_RED);
                Main.getPinHandler().off(Main.NAME_LED2_PROGRESS_YELLOW);
                Main.getPinHandler().off(Main.NAME_LED2_PROGRESS_GREEN);

                Main.getPinHandler().setScheme(Main.NAME_LED1_BTN_RED, "∞:on,250;off,250");
                Main.getPinHandler().setScheme(Main.NAME_LED2_BTN_RED, "∞:on,250;off,250");


                // LED Anzeige, welche die Langeweile der Verteidiger ausdrückt.


                // damit beim Anfang nicht direkt die Shutdown Sirene ertönt
                // UND damit bei einem Overtime die End-Sirene und die Shutdown-Sirene nicht kollidieren
                if (!gameJustResumed && !gameJustStarted && ((FC1GameEvent) messageEvent).getOvertime() < 0) {
                    Main.getPinHandler().setScheme(Main.NAME_SHUTDOWN_SIREN, "1:on,2000;off,0");
                }

                gameJustStarted = false;
                gameJustResumed = false;
                lastAnnouncedMinute = -1;
                lastAnnouncedSecond = -1;
                hotcountdownrunning.set(false);
                coldcountdownrunning.set(false);
            } else if (messageEvent.getEvent() == GameEvent.PREGAME) {
                /***
                 *      ____            ____
                 *     |  _ \ _ __ ___ / ___| __ _ _ __ ___   ___
                 *     | |_) | '__/ _ \ |  _ / _` | '_ ` _ \ / _ \
                 *     |  __/| | |  __/ |_| | (_| | | | | | |  __/
                 *     |_|   |_|  \___|\____|\__,_|_| |_| |_|\___|
                 *
                 */

                Main.enableSettings(true);
                Main.getFrmTest().getBtnClearEvent().setEnabled(false);

                Main.setProgress(BigDecimal.ONE.negate()); // stop progess. -1 beendet alles.


                Main.getPinHandler().off(Main.NAME_SHUTDOWN_SIREN);
                Main.getPinHandler().off(Main.NAME_START_STOP_SIREN);
                Main.getPinHandler().off(Main.NAME_SIREN1);
                Main.getPinHandler().off(Main.NAME_RESPAWN_SIREN);

                Main.getPinHandler().off(Main.NAME_RESPAWN_SIREN);

                Main.getPinHandler().setScheme(Main.NAME_LED1_BTN_RED, "∞:on,1000;off,1000");
                Main.getPinHandler().setScheme(Main.NAME_LED1_BTN_GREEN, "∞:on,1000;off,1000");

                Main.getPinHandler().setScheme(Main.NAME_LED2_BTN_RED, "∞:on,1000;off,1000");
                Main.getPinHandler().setScheme(Main.NAME_LED2_BTN_GREEN, "∞:on,1000;off,1000");

                Main.getPinHandler().setScheme(Main.NAME_LED1_PROGRESS_RED, "∞:on,350;off,3700");
                Main.getPinHandler().setScheme(Main.NAME_LED1_PROGRESS_YELLOW, "∞:off,350;on,350;off,3350");
                Main.getPinHandler().setScheme(Main.NAME_LED1_PROGRESS_GREEN, "∞:off,700;on,350;off,3000");
                Main.getPinHandler().setScheme(Main.NAME_LED2_PROGRESS_RED, "∞:on,350;off,3700");
                Main.getPinHandler().setScheme(Main.NAME_LED2_PROGRESS_YELLOW, "∞:off,350;on,350;off,3350");
                Main.getPinHandler().setScheme(Main.NAME_LED2_PROGRESS_GREEN, "∞:off,700;on,350;off,3000");

                

                statistics.reset();
                lastMinuteAnnounced = false;
                lastAnnouncedMinute = -1;
                lastAnnouncedSecond = -1;
                coldcountdownrunning.set(false);
                hotcountdownrunning.set(false);
                overtime.set(false);

            } else if (messageEvent.getEvent() == GameEvent.DEFENDED) {
                /***
                 *      _____ _             ____        __                _          _
                 *     |  ___| | __ _  __ _|  _ \  ___ / _| ___ _ __   __| | ___  __| |
                 *     | |_  | |/ _` |/ _` | | | |/ _ \ |_ / _ \ '_ \ / _` |/ _ \/ _` |
                 *     |  _| | | (_| | (_| | |_| |  __/  _|  __/ | | | (_| |  __/ (_| |
                 *     |_|   |_|\__,_|\__, |____/ \___|_|  \___|_| |_|\__,_|\___|\__,_|
                 *                    |___/
                 */

                Main.getPinHandler().off(Main.NAME_SHUTDOWN_SIREN);

                setDefendedBlinkScheme();

//                Main.getPinHandler().off(Main.NAME_LED1_BTN_RED);
//                Main.getPinHandler().off(Main.NAME_LED1_BTN_GREEN);
//                Main.getPinHandler().off(Main.NAME_LED2_BTN_RED);
//                Main.getPinHandler().off(Main.NAME_LED2_BTN_GREEN);
//
//                Main.getPinHandler().off(Main.NAME_LED1_PROGRESS_RED);
//                Main.getPinHandler().off(Main.NAME_LED1_PROGRESS_YELLOW);
//                Main.getPinHandler().off(Main.NAME_LED1_PROGRESS_GREEN);
//                Main.getPinHandler().off(Main.NAME_LED2_PROGRESS_RED);
//                Main.getPinHandler().off(Main.NAME_LED2_PROGRESS_YELLOW);
//                Main.getPinHandler().off(Main.NAME_LED2_PROGRESS_GREEN);
//
//                Main.getPinHandler().setScheme(Main.NAME_LED1_PROGRESS_GREEN, "∞:on,500;off,500");
//                Main.getPinHandler().setScheme(Main.NAME_LED2_PROGRESS_GREEN, "∞:on,500;off,500");
//                Main.getPinHandler().setScheme(Main.NAME_LED1_BTN_GREEN, "∞:on,500;off,500");
//                Main.getPinHandler().setScheme(Main.NAME_LED2_BTN_GREEN, "∞:on,500;off,500");

                Main.getPinHandler().setScheme(Main.NAME_START_STOP_SIREN, String.format("1:on,%s;off,0", Main.getConfigs().get(Configs.MBX_STARTGAME_SIRENTIME)));

                String message = Tools.h1(Tools.xx("fc1assault.gamestate." + messageEvent.getEvent()));
                if (overtime.get()) {
                    message += "<h2>SUDDEN DEATH (Overtime)</h2>";
//                    lastStatsSent = statistics.addEvent(new GameEvent(messageEvent, Statistics.GAME_SUDDEN_DEATH));
                }

                Main.getFrmTest().setMessage(message);
                Main.setRespawnTimer("--");

            } else if (messageEvent.getEvent() == GameEvent.EXPLODED) {
                /***
                 *      _____ _            _____     _
                 *     |  ___| | __ _  __ |_   _|_ _| | _____ _ __
                 *     | |_  | |/ _` |/ _` || |/ _` | |/ / _ \ '_ \
                 *     |  _| | | (_| | (_| || | (_| |   <  __/ | | |
                 *     |_|   |_|\__,_|\__, ||_|\__,_|_|\_\___|_| |_|
                 *                    |___/
                 */

                setTakenBlinkScheme();

//                MissionBox.setScheme(MissionBox.MBX_SIREN1, "1;3000,0");
                Main.getPinHandler().setScheme(Main.NAME_START_STOP_SIREN, String.format("1:on,%s;off,0", Main.getConfigs().get(Configs.MBX_STARTGAME_SIRENTIME)));

            } else if (messageEvent.getEvent() == GameEvent.START_GAME) {
                /***
                 *      _____ _                _        _   _
                 *     |  ___| | __ _  __ _   / \   ___| |_(_)_   _____
                 *     | |_  | |/ _` |/ _` | / _ \ / __| __| \ \ / / _ \
                 *     |  _| | | (_| | (_| |/ ___ \ (__| |_| |\ V /  __/
                 *     |_|   |_|\__,_|\__, /_/   \_\___|\__|_| \_/ \___|
                 *                    |___/
                 */
                Main.enableSettings(false);

                Main.getPinHandler().off(Main.NAME_LED1_PROGRESS_RED);
                Main.getPinHandler().off(Main.NAME_LED1_PROGRESS_YELLOW);
                Main.getPinHandler().off(Main.NAME_LED1_PROGRESS_GREEN);
                Main.getPinHandler().off(Main.NAME_LED2_PROGRESS_RED);
                Main.getPinHandler().off(Main.NAME_LED2_PROGRESS_YELLOW);
                Main.getPinHandler().off(Main.NAME_LED2_PROGRESS_GREEN);

                // the starting siren

                Main.getPinHandler().setScheme(Main.NAME_START_STOP_SIREN, String.format("1:on,%s;off,0", Main.getConfigs().get(Configs.MBX_STARTGAME_SIRENTIME)));

                gameJustStarted = true;
//                gameJustResumed = false;


            }
        };


        farcryAssaultThread = new Farcry1AssaultThread(gameTimeListener, gameModeListener, Long.parseLong(Main.getConfigs().get(Configs.FCY_GAMETIME)), Long.parseLong(Main.getConfigs().get(Configs.FCY_TIME2CAPTURE)), Long.parseLong(Main.getConfigs().get(Configs.FCY_RESPAWN_INTERVAL)));


        /***
         *      ____  _         ____          _    ____ ____ ___ ___
         *     | __ )| |_ _ __ |  _ \ ___  __| |  / ___|  _ \_ _/ _ \
         *     |  _ \| __| '_ \| |_) / _ \/ _` | | |  _| |_) | | | | |
         *     | |_) | |_| | | |  _ <  __/ (_| | | |_| |  __/| | |_| |
         *     |____/ \__|_| |_|_| \_\___|\__,_|  \____|_|  |___\___/
         *
         */
        Main.getBtnRed().addListener((GpioPinListenerDigital) event -> {
//            getLogger().debug(event);
            Main.getFrmTest().setButtonTestLabel("red", event.getState() == PinState.LOW); // for debugging
            if (event.getState() == PinState.LOW) {
                getLogger().debug("GPIO RedButton down");
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
            getLogger().debug("SWING RedButton clicked");
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
            getLogger().debug(ToStringBuilder.reflectionToString(event.getState()));
            Main.getFrmTest().setButtonTestLabel("green", event.getState() == PinState.LOW); // for debugging
            if (event.getState() == PinState.LOW) {
                getLogger().debug("GPIO GreenButton down");

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
            getLogger().debug("SWING GreenButton clicked");
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
            getLogger().debug(ToStringBuilder.reflectionToString(event.getState()));
            Main.getFrmTest().setButtonTestLabel("start", event.getState() == PinState.LOW); // for debugging
            if (!Main.isGameStartable()) return;
            if (farcryAssaultThread.isPausing()) return;
            if (event.getState() == PinState.LOW) {
                getLogger().debug("GPIO Start/Stop down");
                if (farcryAssaultThread.getGameEvent() == GameEvent.PREGAME) {
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
            getLogger().debug("btnGameStartStop");
            if (farcryAssaultThread.getGameEvent() == GameEvent.PREGAME) {
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
            getLogger().debug(ToStringBuilder.reflectionToString(event.getState()));
            Main.getFrmTest().setButtonTestLabel("pause", event.getState() == PinState.LOW); // for debugging
            if (event.getState() == PinState.LOW) {
                getLogger().debug("GPIO BtnPause down");
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
            getLogger().debug("btnPause - on Screen");
            farcryAssaultThread.togglePause();
        });

    } // constructor

    public void setTakenBlinkScheme() {
        Main.getPinHandler().off(Main.NAME_LED1_BTN_RED);
        Main.getPinHandler().off(Main.NAME_LED1_BTN_GREEN);
        Main.getPinHandler().off(Main.NAME_LED2_BTN_RED);
        Main.getPinHandler().off(Main.NAME_LED2_BTN_GREEN);

        Main.getPinHandler().off(Main.NAME_LED1_PROGRESS_RED);
        Main.getPinHandler().off(Main.NAME_LED1_PROGRESS_YELLOW);
        Main.getPinHandler().off(Main.NAME_LED1_PROGRESS_GREEN);
        Main.getPinHandler().off(Main.NAME_LED2_PROGRESS_RED);
        Main.getPinHandler().off(Main.NAME_LED2_PROGRESS_YELLOW);
        Main.getPinHandler().off(Main.NAME_LED2_PROGRESS_GREEN);

        Main.getPinHandler().setScheme(Main.NAME_LED1_PROGRESS_RED, "∞:on,500;off,500");
        Main.getPinHandler().setScheme(Main.NAME_LED1_PROGRESS_RED, "∞:on,500;off,500");
        Main.getPinHandler().setScheme(Main.NAME_LED1_BTN_RED, "∞:on,500;off,500");
        Main.getPinHandler().setScheme(Main.NAME_LED2_BTN_RED, "∞:on,500;off,500");
    }


    public void setDefendedBlinkScheme() {
        Main.getPinHandler().off(Main.NAME_LED1_BTN_RED);
        Main.getPinHandler().off(Main.NAME_LED1_BTN_GREEN);
        Main.getPinHandler().off(Main.NAME_LED2_BTN_RED);
        Main.getPinHandler().off(Main.NAME_LED2_BTN_GREEN);

        Main.getPinHandler().off(Main.NAME_LED1_PROGRESS_RED);
        Main.getPinHandler().off(Main.NAME_LED1_PROGRESS_YELLOW);
        Main.getPinHandler().off(Main.NAME_LED1_PROGRESS_GREEN);
        Main.getPinHandler().off(Main.NAME_LED2_PROGRESS_RED);
        Main.getPinHandler().off(Main.NAME_LED2_PROGRESS_YELLOW);
        Main.getPinHandler().off(Main.NAME_LED2_PROGRESS_GREEN);

        Main.getPinHandler().setScheme(Main.NAME_LED1_PROGRESS_GREEN, "∞:on,500;off,500");
        Main.getPinHandler().setScheme(Main.NAME_LED2_PROGRESS_GREEN, "∞:on,500;off,500");
        Main.getPinHandler().setScheme(Main.NAME_LED1_BTN_GREEN, "∞:on,500;off,500");
        Main.getPinHandler().setScheme(Main.NAME_LED2_BTN_GREEN, "∞:on,500;off,500");
    }


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

    public void setRevertEvent(FC1SavePoint revertEvent) {
        farcryAssaultThread.setSavePoint(revertEvent);
    }
}
