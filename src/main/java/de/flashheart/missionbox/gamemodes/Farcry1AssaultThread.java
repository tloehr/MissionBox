package de.flashheart.missionbox.gamemodes;

import de.flashheart.missionbox.Main;
import de.flashheart.missionbox.events.FC1GameEvent;
import de.flashheart.missionbox.events.GameEvent;
import de.flashheart.missionbox.events.MessageListener;
import de.flashheart.missionbox.events.Statistics;
import de.flashheart.missionbox.misc.Configs;
import de.flashheart.missionbox.misc.Tools;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Dieser Thread läuft wie ein Motor in Zyklen ab. Jeder Zyklus dauert <b>millispercycle</b> millisekunden.
 * Nach <b>maxcycles</b> Zyklen ist das Spiel vorbei. <b>TIME2CAP</b> ist die Anzahl der Sekunden, die es braucht, bis die Flagge genommen wurde. (Wenn sie vorher nicht deaktiviert wurde).
 * <p>
 * <p>
 * Wir müssen das so sehen, dass der GameThread (interface) die GameEvents auslöst (bemerkt), die zeitabhängig sind.
 * Der übergeordnete bzw. aufrufende GameMode (interface) die Button Ereignisse auslöst bzw. bemerkt.
 * <p>
 * Der Thread informiert den GameMode über EventListener, wenn etwas zu melden ist.
 */
public class Farcry1AssaultThread implements Runnable, GameThread {
    final Logger logger = Logger.getLogger(getClass());
    private final Thread thread;
    final ReentrantLock lock;

    private String gameEvent, previousGameState, resumeToState = "null";
    private long resumeInterval;

    // makes sure that the time event is only triggered once a second
    private long threadcycles = 0;

    private final long millispercycle = 50;
    private long lastRemainingTime = 0l;

    /**
     * systemzeit des starts. wird nur gebraucht um den gametimer zu setzen. das er bei 0 anfängt muss er relativ zur Startzeit berechnet werden.
     */
    private long starttime = -1l;


    private long gametimer = 0l; // wie lange läuft das Spiel schon ?
    private long timeWhenTheFlagWasActivated = -1l; // wann wurde die Flagge zuletzt aktiviert. -1l heisst, nicht aktiv.
    private long maxgametime = 0l; // wie lange kann dieses Spiel maximal laufen
    private long capturetime = 0l; // wie lange muss die Flagge gehalten werden bis sie erobert wurde ?
    private long respawninterval = 0l; // wie lange zwischen zwei Respawns. 0l, wenn keine Respawns erwünscht.


    private long pausingSince = -1l;
    private long resumingSince = -1l;
    private long lastrespawn = 0l; // wann war das letzte Respawn Signal. Das erste mal ist immer bei Spielstart, daher 0l.


//    public static final String[] GAMSTATS = new String[]{"PREPGAME", "FLAGACTV", "FLAGCOLD", "FLAG_HOT", "FLAGTAKN", "FLAGDFND", "GNGPAUSE", "PAUSING", "GNGRESUM", "RESUMED "};

//    DateFormat formatter = new SimpleDateFormat("mm:ss");

    /**
     * hiermit werden beliebige Texte verschickt, die dann auf Displays weitergeleitet werden können.
     */
    private final EventListenerList textMessageList;
    /**
     * die Spielzeit wird regelmässig als Event gemeldet.
     */
    private final EventListenerList gameTimerList;
    /**
     * Wenn irgendwelche Countdowns laufen, dann wird der Fortschritt als Event gesendet.
     */
//    private final EventListenerList percentageList;
    /**
     * Veränderungen im Spielzustand der Box werden über diese Liste verschickt.
     */
    private final EventListenerList gameModeList;
    /**
     * Wenn in der Pause ein Rücksprungsziel ausgewählt wurde, dann steht das hier drin.
     * Wenn nicht, ist das immer NULL.
     * Auch nach einem revert wird dieser Event wieder auf NULL gesetzt.
     */
    private FC1SavePoint savePoint = null;
    private boolean addEventToList = true;
    private int running_match_id;


    /**
     * @param messageListener
     * @param gameTimerListener
     * @param gameModeListener
     * @param maxgametimeInMins - in Minuten
     * @param capturetimeInSecs - in Sekunden
     */

    public Farcry1AssaultThread(MessageListener messageListener, MessageListener gameTimerListener, MessageListener gameModeListener, long maxgametimeInMins, long capturetimeInSecs, long respawnintervalInSecs) {
        super();

        lock = new ReentrantLock();

        // todo: das muss noch auf die config seite
        resumeInterval = Long.parseLong(Main.getConfigs().get(Configs.MBX_RESUME_TIME));
        running_match_id = Integer.parseInt(Main.getConfigs().get(Configs.MATCHID)) + 1;
        Main.getConfigs().put(Configs.MATCHID, Integer.toString(running_match_id));

        setRespawninterval(respawnintervalInSecs);
        setMaxgametime(maxgametimeInMins);
        setCapturetime(capturetimeInSecs);

        thread = new Thread(this);
        logger.setLevel(Main.getLogLevel());

        textMessageList = new EventListenerList();
        gameTimerList = new EventListenerList();
//        percentageList = new EventListenerList();
        gameModeList = new EventListenerList();

        textMessageList.add(MessageListener.class, messageListener);
        gameTimerList.add(MessageListener.class, gameTimerListener);
//        percentageList.add(MessageListener.class, percentageListener);
        gameModeList.add(MessageListener.class, gameModeListener);

        previousGameState = Statistics.GAME_NON_EXISTENT;

        prepareGame();
    }

    public boolean isPausing() {
        return (gameEvent == Statistics.GAME_PAUSING || gameEvent == Statistics.GAME_GOING_TO_PAUSE);
    }

    public void setFlagHot(boolean hot) {
        if (isPausing()) return; // eigentlich brauche ich diese Abfrage nicht, weil dass durch die Konstruktion schon
        // bewältigt wird. Aber so ist es schön einheitlich.
        if (hot) {
            if (gameEvent == Statistics.GAME_FLAG_COLD) {
                timeWhenTheFlagWasActivated = gametimer;
                setGameEvent(Statistics.GAME_FLAG_HOT);
            }
        } else {
            if (gameEvent == Statistics.GAME_FLAG_HOT) {
                timeWhenTheFlagWasActivated = -1l;
                setGameEvent(Statistics.GAME_FLAG_COLD);
            }
        }
    }

    /**
     * wird gebraucht, wenn während eines PREGAMES die Zeiten geändert werden
     *
     * @param maxgametime - in Minuten
     */
    public void setMaxgametime(long maxgametime) {
        this.maxgametime = maxgametime * 60000l;
    }

    public void setRespawninterval(long respawninterval) {
        this.respawninterval = respawninterval * 1000l;
    }

    /**
     * wird gebraucht, wenn während eines PREGAMES die Zeiten geändert werden
     *
     * @param capturetime - in Sekunden
     */
    public void setCapturetime(long capturetime) {
        this.capturetime = capturetime * 1000l;
    }

    /**
     * setGameEvent wird (indirekt) von außen aufgerufen, wenn z.B. ein Knopf gedrückt wird.
     * Es wird aber auch von innerhalb dieser Klasse aufgerufen (das ist sogar der Regelfall),
     * wenn bestimmte Zeiten abgelaufen sind.
     *
     * @param state
     */
    private void setGameEvent(String state) {
        lock.lock();
        try {
            this.gameEvent = state;
            logger.debug("setting gamestate to: " + gameEvent);
            lastRemainingTime = getRemaining();
            
            if (gameEvent != previousGameState) {

                fireMessage(gameModeList, new FC1GameEvent(this, running_match_id, gameEvent, starttime, gametimer, timeWhenTheFlagWasActivated, maxgametime, capturetime, pausingSince, resumingSince, lastrespawn, respawninterval, resumeInterval, lastRemainingTime));

                switch (gameEvent) {
                    case Statistics.GAME_PRE_GAME: {
                        logger.info("\n  ____  ____  _____ ____    _    __  __ _____ \n" +
                                " |  _ \\|  _ \\| ____/ ___|  / \\  |  \\/  | ____|\n" +
                                " | |_) | |_) |  _|| |  _  / _ \\ | |\\/| |  _|  \n" +
                                " |  __/|  _ <| |__| |_| |/ ___ \\| |  | | |___ \n" +
                                " |_|   |_| \\_\\_____\\____/_/   \\_\\_|  |_|_____|\n" +
                                "                                              ");

                        starttime = -1l;
                        timeWhenTheFlagWasActivated = -1l;

                        if (running_match_id == 0) {
                            running_match_id = Integer.parseInt(Main.getConfigs().get(Configs.MATCHID)) + 1;
                            Main.getConfigs().put(Configs.MATCHID, Integer.toString(running_match_id));
                        }

                        fireMessage(textMessageList, new GameEvent(this, gameEvent, running_match_id, gametimer, lastRemainingTime));
                        Main.getFrmTest().clearEvents();
                        break;
                    }
                    case Statistics.GAME_FLAG_ACTIVE: { // hier wird das Spiel gestartet
                        logger.info("\n  _____ _        _    ____      _    ____ _____ _____     _______ \n" +
                                " |  ___| |      / \\  / ___|    / \\  / ___|_   _|_ _\\ \\   / / ____|\n" +
                                " | |_  | |     / _ \\| |  _    / _ \\| |     | |  | | \\ \\ / /|  _|  \n" +
                                " |  _| | |___ / ___ \\ |_| |  / ___ \\ |___  | |  | |  \\ V / | |___ \n" +
                                " |_|   |_____/_/   \\_\\____| /_/   \\_\\____| |_| |___|  \\_/  |_____|\n" +
                                "                                                                  ");
                        gametimer = 0l;
                        lastrespawn = 0l;
                        addEventToList = true;
                        running_match_id = Integer.parseInt(Main.getConfigs().get(Configs.MATCHID)) + 1;
                        Main.getConfigs().put(Configs.MATCHID, Integer.toString(running_match_id));
                        starttime = System.currentTimeMillis();
                        fireMessage(textMessageList, new GameEvent(this, gameEvent, running_match_id, gametimer, lastRemainingTime));
                        setGameEvent(Statistics.GAME_FLAG_COLD);
                        break;
                    }
                    case Statistics.GAME_FLAG_HOT: {
                        logger.info("\n  _____ _        _    ____   _   _  ___ _____ \n" +
                                " |  ___| |      / \\  / ___| | | | |/ _ \\_   _|\n" +
                                " | |_  | |     / _ \\| |  _  | |_| | | | || |  \n" +
                                " |  _| | |___ / ___ \\ |_| | |  _  | |_| || |  \n" +
                                " |_|   |_____/_/   \\_\\____| |_| |_|\\___/ |_|  \n" +
                                "                                              ");

                        // es gibt nur eine Situation, wenn kein neuer Event erzeugt werden soll,
                        // nämlich, wenn die Pause gerade vorbei ist, aber kein Revert ausgewählt wurde.
                        // dann soll alles normal weiter laufen, wie VOR der Pause.
                        if (addEventToList) {
                            Main.getFrmTest().addGameEvent(new FC1SavePoint(new FC1GameEvent(this, running_match_id, gameEvent, starttime, gametimer, timeWhenTheFlagWasActivated, maxgametime, capturetime, pausingSince, resumingSince, lastrespawn, respawninterval, resumeInterval, getRemaining()), new ImageIcon((getClass().getResource("/artwork/ledred32.png")))), lastRemainingTime);
                        }
                        addEventToList = true;

                        fireMessage(textMessageList, new GameEvent(this, gameEvent, running_match_id, gametimer, lastRemainingTime));
                        break;
                    }

                    case Statistics.GAME_FLAG_COLD: {
                        logger.info("\n  _____ _        _    ____    ____ ___  _     ____  \n" +
                                " |  ___| |      / \\  / ___|  / ___/ _ \\| |   |  _ \\ \n" +
                                " | |_  | |     / _ \\| |  _  | |  | | | | |   | | | |\n" +
                                " |  _| | |___ / ___ \\ |_| | | |__| |_| | |___| |_| |\n" +
                                " |_|   |_____/_/   \\_\\____|  \\____\\___/|_____|____/ \n" +
                                "                                                    ");
                        if (addEventToList) {
                            Main.getFrmTest().addGameEvent(new FC1SavePoint(new FC1GameEvent(this, running_match_id, gameEvent, starttime, gametimer, timeWhenTheFlagWasActivated, maxgametime, capturetime, pausingSince, resumingSince, lastrespawn, respawninterval, resumeInterval, getRemaining()), new ImageIcon((getClass().getResource("/artwork/ledgreen32.png")))), lastRemainingTime);
                        }
                        addEventToList = true;

                        fireMessage(textMessageList, new GameEvent(this, gameEvent, running_match_id, gametimer, lastRemainingTime));
                        break;
                    }
                    case Statistics.GAME_OUTCOME_FLAG_TAKEN: {
                        logger.info("\n  _____ _        _    ____   _____  _    _  _______ _   _ \n" +
                                " |  ___| |      / \\  / ___| |_   _|/ \\  | |/ / ____| \\ | |\n" +
                                " | |_  | |     / _ \\| |  _    | | / _ \\ | ' /|  _| |  \\| |\n" +
                                " |  _| | |___ / ___ \\ |_| |   | |/ ___ \\| . \\| |___| |\\  |\n" +
                                " |_|   |_____/_/   \\_\\____|   |_/_/   \\_\\_|\\_\\_____|_| \\_|\n" +
                                "                                                          ");
                        Main.getFrmTest().addGameEvent(new FC1SavePoint(new FC1GameEvent(this, running_match_id, gameEvent, starttime, gametimer, timeWhenTheFlagWasActivated, maxgametime, capturetime, pausingSince, resumingSince, lastrespawn, respawninterval, resumeInterval, getRemaining()), new ImageIcon((getClass().getResource("/artwork/rocket32.png")))), 0);
                        addEventToList = true;
                        fireMessage(textMessageList, new GameEvent(this, gameEvent, running_match_id, gametimer, lastRemainingTime));
                        running_match_id = 0;
                        break;
                    }
                    case Statistics.GAME_OUTCOME_FLAG_DEFENDED: {
                        logger.info("\n  _____ _        _    ____   ____  _____ _____ _____ _   _ ____  _____ ____  \n" +
                                " |  ___| |      / \\  / ___| |  _ \\| ____|  ___| ____| \\ | |  _ \\| ____|  _ \\ \n" +
                                " | |_  | |     / _ \\| |  _  | | | |  _| | |_  |  _| |  \\| | | | |  _| | | | |\n" +
                                " |  _| | |___ / ___ \\ |_| | | |_| | |___|  _| | |___| |\\  | |_| | |___| |_| |\n" +
                                " |_|   |_____/_/   \\_\\____| |____/|_____|_|   |_____|_| \\_|____/|_____|____/ \n" +
                                "                                                                             ");
                        Main.getFrmTest().addGameEvent(new FC1SavePoint(new FC1GameEvent(this, running_match_id, gameEvent, starttime, gametimer, timeWhenTheFlagWasActivated, maxgametime, capturetime, pausingSince, resumingSince, lastrespawn, respawninterval, resumeInterval, getRemaining()), new ImageIcon((getClass().getResource("/artwork/shield32.png")))), 0);
                        addEventToList = true;
//                        fireMessage(gameTimerList, new FC1DetailsMessageEvent(this, gameEvent, starttime, gametimer, timeWhenTheFlagWasActivated, maxgametime, capturetime, pausingSince, resumingSince, lastrespawn, respawninterval, resumeInterval, lastRemainingTime));
                        logger.debug("defended gametimer: " + gametimer);
                        fireMessage(textMessageList, new GameEvent(this, gameEvent, running_match_id, gametimer, lastRemainingTime));
                        running_match_id = 0;
                        break;
                    }
                    case Statistics.GAME_GOING_TO_PAUSE: {
                        pausingSince = System.currentTimeMillis();
                        resumeToState = previousGameState; // falls doch kein revert gebraucht wird
                        Main.getPinHandler().pause();
                        fireMessage(textMessageList, new GameEvent(this, gameEvent, running_match_id, gametimer, lastRemainingTime));
                        setGameEvent(Statistics.GAME_PAUSING);
                        break;
                    }
                    case Statistics.GAME_PAUSING: {
                        fireMessage(textMessageList, new GameEvent(this, gameEvent, running_match_id, gametimer, lastRemainingTime));
                        break;
                    }
                    case Statistics.GAME_GOING_TO_RESUME: {
                        resumingSince = System.currentTimeMillis();
                        fireMessage(textMessageList, new GameEvent(this, gameEvent, running_match_id, gametimer, lastRemainingTime));
                        Main.getFrmTest().setToPauseMode(false);
                        break;
                    }
                    case Statistics.GAME_RESUMED: {

                        // Korrektur der Pausen und Resume Zeit
                        long pause_period = System.currentTimeMillis() - pausingSince;      // damit ist auch die resume zeit inbegriffen.
                        logger.debug("diese Pause (inklusive dem resume) dauerte: " + Tools.formatLongTime(pause_period));

                        resumingSince = -1l;
                        pausingSince = -1l;

                        // wichtig, sonst werden weitere events nach beenden der Pause OHNE Revert hinzugefügt.
                        addEventToList = savePoint != null;

                        // wenn es einen Event gibt, zu dem Zurückgesprungen werden soll, dann
                        // muss er jetzt berücksichtigt werden.
                        if (savePoint != null) {
                            logger.debug("\n" +
                                    "  ____  _______     _______ ____ _____   _______     _______ _   _____\n" +
                                    " |  _ \\| ____\\ \\   / / ____|  _ \\_   _| | ____\\ \\   / / ____| \\ | |_   _|\n" +
                                    " | |_) |  _|  \\ \\ / /|  _| | |_) || |   |  _|  \\ \\ / /|  _| |  \\| | | |  \n" +
                                    " |  _ <| |___  \\ V / | |___|  _ < | |   | |___  \\ V / | |___| |\\  | | |  \n" +
                                    " |_| \\_\\_____|  \\_/  |_____|_| \\_\\|_|   |_____|  \\_/  |_____|_| \\_| |_|  \n" +
                                    "                                                                         ");
                            logger.debug(savePoint.toString());

                            resumeToState = savePoint.getMessageEvent().getEvent();

                            // die Pause und das Resume muss hier nicht berechnet werden. Durch currentTimeMillis() ist das nicht nötig.
                            // nur wenn die Pause OHNE Revert beendet wird, muss die Starttime entsprechend verschoben werden.
                            starttime = System.currentTimeMillis() - savePoint.getMessageEvent().getGametime() - savePoint.getEventDuration();

                            // das und die starttime reichen aus um die restlichen timer zu errechnen, was dann in run() aus passiert.
                            lastrespawn = savePoint.getMessageEvent().getLastrespawn();


                            timeWhenTheFlagWasActivated = -1l;
                            if (resumeToState == Statistics.GAME_FLAG_HOT) {
                                timeWhenTheFlagWasActivated = savePoint.getMessageEvent().getTimeWhenTheFlagWasActivated();
                                logger.debug("flagactivate ist nun: " + Tools.formatLongTime(timeWhenTheFlagWasActivated));
                            }

                            Main.getFrmTest().setRevertEvent(null);
                            savePoint = null;
                            Main.getFrmTest().clearEvents(); // jedes revert geht nur einmal, danach ist die Liste leer.
                        } else {
                            // pause_perios enthält auch die Zeit für den Resume
                            starttime = starttime + pause_period;
                        }

                        fireMessage(textMessageList, new GameEvent(this, gameEvent, running_match_id, gametimer, lastRemainingTime));
                        Main.getPinHandler().resume();

                        String resume = resumeToState;
                        resumeToState = "null"; // das muss hier schon erledigt werden, sonst stolpere ich da beim nächsten Durchlauf drüber.
                        // durch verwendung des LOKALEN resume 

                        setGameEvent(resume);
                        break;
                    }

                    default: {
                        fireMessage(textMessageList, new GameEvent(this, gameEvent, running_match_id, gametimer, lastRemainingTime));
                    }
                }

                previousGameState = gameEvent;
            }
        } finally {
            lock.unlock();
        }
    }


    @Override
    public void prepareGame() {
        previousGameState = Statistics.GAME_NON_EXISTENT;
        setGameEvent(Statistics.GAME_PRE_GAME);
    }

    @Override
    public void startGame() {
        setGameEvent(Statistics.GAME_FLAG_ACTIVE);
    }

    @Override
    public void quitGame() {
        thread.interrupt();
    }

    @Override
    public void togglePause() {
        if (isPausing()) {
            setGameEvent(Statistics.GAME_GOING_TO_RESUME);
        } else {
            // DEFENDED, weil das könnte ja ein getroffener Verteidiger noch schnell entschärft haben.
            // Wenn er das im OVERTIME macht, dann ist ein UNDO nötig.
            if (isGameRunning() || gameEvent == Statistics.GAME_OUTCOME_FLAG_DEFENDED) {
                Main.getFrmTest().setToPauseMode(true);
                setGameEvent(Statistics.GAME_GOING_TO_PAUSE);
            }
        }
    }


    protected synchronized void fireMessage(EventListenerList listeners, GameEvent textMessage) {
        for (MessageListener listener : listeners.getListeners(MessageListener.class)) {
            listener.messageReceived(textMessage);
        }
    }

    public String getGameEvent() {
        return gameEvent;
    }

    public long getRemaining() {
        long endtime = maxgametime;
        if (gameEvent == Statistics.GAME_FLAG_HOT) {
            endtime = timeWhenTheFlagWasActivated + capturetime;
        }
        return endtime - gametimer;
    }

    public boolean isGameRunning() {
        return gameEvent == Statistics.GAME_FLAG_ACTIVE || gameEvent == Statistics.GAME_FLAG_HOT || gameEvent == Statistics.GAME_FLAG_COLD;
    }
//
//    public boolean isGameJustEnded() {
//        return gameEvent == Statistics.GAME_OUTCOME_FLAG_TAKEN || gameEvent == Statistics.GAME_OUTCOME_FLAG_DEFENDED;
//    }


    // Die bewertung der Spielsituation erfolgt in Zyklen.
    public void run() {

        while (!thread.isInterrupted()) {

            if (isGameRunning()) {
                long now = System.currentTimeMillis();

                gametimer = now - starttime;
                lastRemainingTime = getRemaining();

                fireMessage(gameTimerList, new FC1GameEvent(this, running_match_id, gameEvent, starttime, gametimer, timeWhenTheFlagWasActivated, maxgametime, capturetime, pausingSince, resumingSince, lastrespawn, respawninterval, resumeInterval, lastRemainingTime));
                if (lastrespawn + respawninterval <= gametimer) {
                    lastrespawn = gametimer;
                }

            } else if (pausingSince >= 0) {
                fireMessage(gameTimerList, new FC1GameEvent(this, running_match_id, gameEvent, starttime, gametimer, timeWhenTheFlagWasActivated, maxgametime, capturetime, pausingSince, resumingSince, lastrespawn, respawninterval, resumeInterval, lastRemainingTime));
            } else if (gameEvent == Statistics.GAME_PRE_GAME) {
                fireMessage(gameTimerList, new FC1GameEvent(this, running_match_id, gameEvent, starttime, gametimer, timeWhenTheFlagWasActivated, maxgametime, capturetime, pausingSince, resumingSince, lastrespawn, respawninterval, resumeInterval, lastRemainingTime));
            }


            try {
                if (gameEvent == Statistics.GAME_FLAG_COLD) {
                    if (getRemaining() <= 0l) {
                        setGameEvent(Statistics.GAME_OUTCOME_FLAG_DEFENDED);
                    }
                }

                if (gameEvent == Statistics.GAME_FLAG_HOT) {
                    if (getRemaining() <= 0l) {
                        setGameEvent(Statistics.GAME_OUTCOME_FLAG_TAKEN);
                    }
                }

                if (gameEvent == Statistics.GAME_GOING_TO_RESUME) {
                    // um einen Countdown zu zeigen, bevor es weiter geht.
                    // logger.debug("resuming in " + new DateTime(resumeInterval - (System.currentTimeMillis() - resumingSince), DateTimeZone.UTC).toString("mm:ss"));
                    if (System.currentTimeMillis() - resumingSince >= resumeInterval) {
                        setGameEvent(Statistics.GAME_RESUMED);
                    } else {
                        fireMessage(gameTimerList, new FC1GameEvent(this, running_match_id, gameEvent, starttime, gametimer, timeWhenTheFlagWasActivated, maxgametime, capturetime, pausingSince, resumingSince, lastrespawn, respawninterval, resumeInterval, lastRemainingTime));
                    }
                }

                Thread.sleep(millispercycle);
            } catch (InterruptedException ie) {
                logger.debug(this + " interrupted!");
            }
        }
    }


    public void setSavePoint(FC1SavePoint savePoint) {
        this.savePoint = savePoint;
    }
}
