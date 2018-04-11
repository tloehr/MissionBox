package gamemodes;

import interfaces.FC1DetailsMessageEvent;
import interfaces.MessageEvent;
import interfaces.MessageListener;
import main.Main;
import misc.Tools;
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

    private int gameState, previousGameState, resumeToState = -1;
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

    public static final int GAME_NON_EXISTENT = -1;
    public static final int GAME_PRE_GAME = 0;
    public static final int GAME_FLAG_ACTIVE = 1;
    public static final int GAME_FLAG_COLD = 2;
    public static final int GAME_FLAG_HOT = 3;
    public static final int GAME_OUTCOME_FLAG_TAKEN = 4;
    public static final int GAME_OUTCOME_FLAG_DEFENDED = 5;
    public static final int GAME_GOING_TO_PAUSE = 6;
    public static final int GAME_PAUSING = 7; // Box pausiert
    public static final int GAME_GOING_TO_RESUME = 8;
    public static final int GAME_RESUMED = 9; // unmittelbar vor der Spielwiederaufnahme

    public static final String[] GAMSTATS = new String[]{"PREPGAME", "FLAGACTV", "FLAGCOLD", "FLAG_HOT", "FLAGTAKN", "FLAGDFND", "GNGPAUSE", "PAUSING", "GNGRESUM", "RESUMED "};

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
    private Farcry1GameEvent revertEvent = null;
    private boolean addEventToList = true;


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
        resumeInterval = Long.parseLong(Main.getConfig(Main.MBX_RESUME_TIME));

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

        previousGameState = GAME_NON_EXISTENT;

        prepareGame();
    }

    public boolean isPausing() {
        return (gameState == GAME_PAUSING || gameState == GAME_GOING_TO_PAUSE);
    }

    public void setFlagHot(boolean hot) {
        if (isPausing()) return; // eigentlich brauche ich diese Abfrage nicht, weil dass durch die Konstruktion schon
        // bewältigt wird. Aber so ist es schön einheitlich.
        if (hot) {
            if (gameState == GAME_FLAG_COLD) {
                timeWhenTheFlagWasActivated = gametimer;
                setGameState(GAME_FLAG_HOT);
            }
        } else {
            if (gameState == GAME_FLAG_HOT) {
                timeWhenTheFlagWasActivated = -1l;
                setGameState(GAME_FLAG_COLD);
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
     * setGameState wird (indirekt) von außen aufgerufen, wenn z.B. ein Knopf gedrückt wird.
     * Es wird aber auch von innerhalb dieser Klasse aufgerufen (das ist sogar der Regelfall),
     * wenn bestimmte Zeiten abgelaufen sind.
     *
     * @param state
     */
    private void setGameState(int state) {
        lock.lock();
        try {
            this.gameState = state;
            logger.debug("setting gamestate to: " + GAMSTATS[gameState]);
            logger.debug("gametimer is now: " + Tools.formatLongTime(getRemaining()));

            if (gameState != previousGameState) {

                fireMessage(gameModeList, new FC1DetailsMessageEvent(this, gameState, starttime, gametimer, timeWhenTheFlagWasActivated, maxgametime, capturetime, pausingSince, resumingSince, lastrespawn, respawninterval, resumeInterval, lastRemainingTime));

                switch (gameState) {
                    case GAME_PRE_GAME: {
                        logger.info("\n  ____  ____  _____ ____    _    __  __ _____ \n" +
                                " |  _ \\|  _ \\| ____/ ___|  / \\  |  \\/  | ____|\n" +
                                " | |_) | |_) |  _|| |  _  / _ \\ | |\\/| |  _|  \n" +
                                " |  __/|  _ <| |__| |_| |/ ___ \\| |  | | |___ \n" +
                                " |_|   |_| \\_\\_____\\____/_/   \\_\\_|  |_|_____|\n" +
                                "                                              ");

                        starttime = -1l;
                        timeWhenTheFlagWasActivated = -1l;
//                        MissionBox.shutdownEverything();
                        fireMessage(textMessageList, new MessageEvent(this, gameState, "assault.gamestate.pre.game"));
                        Main.getFrmTest().clearEvents();
                        break;
                    }
                    case GAME_FLAG_ACTIVE: { // hier wird das Spiel gestartet
                        logger.info("\n  _____ _        _    ____      _    ____ _____ _____     _______ \n" +
                                " |  ___| |      / \\  / ___|    / \\  / ___|_   _|_ _\\ \\   / / ____|\n" +
                                " | |_  | |     / _ \\| |  _    / _ \\| |     | |  | | \\ \\ / /|  _|  \n" +
                                " |  _| | |___ / ___ \\ |_| |  / ___ \\ |___  | |  | |  \\ V / | |___ \n" +
                                " |_|   |_____/_/   \\_\\____| /_/   \\_\\____| |_| |___|  \\_/  |_____|\n" +
                                "                                                                  ");
                        gametimer = 0l;
                        lastrespawn = 0l;
                        addEventToList = true;
                        starttime = System.currentTimeMillis();
                        fireMessage(textMessageList, new MessageEvent(this, gameState, "assault.gamestate.flag.is.active"));
                        setGameState(GAME_FLAG_COLD);
                        break;
                    }
                    case GAME_FLAG_HOT: {
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
                            Main.getFrmTest().addGameEvent(new Farcry1GameEvent(new FC1DetailsMessageEvent(this, gameState, starttime, gametimer, timeWhenTheFlagWasActivated, maxgametime, capturetime, pausingSince, resumingSince, lastrespawn, respawninterval, resumeInterval, getRemaining()), new ImageIcon((getClass().getResource("/artwork/ledred32.png")))), lastRemainingTime);
                        }
                        addEventToList = true;

                        fireMessage(textMessageList, new MessageEvent(this, gameState, "assault.gamestate.flag.is.hot"));
                        break;
                    }

                    case GAME_FLAG_COLD: {
                        logger.info("\n  _____ _        _    ____    ____ ___  _     ____  \n" +
                                " |  ___| |      / \\  / ___|  / ___/ _ \\| |   |  _ \\ \n" +
                                " | |_  | |     / _ \\| |  _  | |  | | | | |   | | | |\n" +
                                " |  _| | |___ / ___ \\ |_| | | |__| |_| | |___| |_| |\n" +
                                " |_|   |_____/_/   \\_\\____|  \\____\\___/|_____|____/ \n" +
                                "                                                    ");
                        if (addEventToList) {
                            Main.getFrmTest().addGameEvent(new Farcry1GameEvent(new FC1DetailsMessageEvent(this, gameState, starttime, gametimer, timeWhenTheFlagWasActivated, maxgametime, capturetime, pausingSince, resumingSince, lastrespawn, respawninterval, resumeInterval, getRemaining()), new ImageIcon((getClass().getResource("/artwork/ledgreen32.png")))), lastRemainingTime);
                        }
                        addEventToList = true;

                        fireMessage(textMessageList, new MessageEvent(this, gameState, "assault.gamestate.flag.is.cold"));
                        break;
                    }
                    case GAME_OUTCOME_FLAG_TAKEN: {
                        logger.info("\n  _____ _        _    ____   _____  _    _  _______ _   _ \n" +
                                " |  ___| |      / \\  / ___| |_   _|/ \\  | |/ / ____| \\ | |\n" +
                                " | |_  | |     / _ \\| |  _    | | / _ \\ | ' /|  _| |  \\| |\n" +
                                " |  _| | |___ / ___ \\ |_| |   | |/ ___ \\| . \\| |___| |\\  |\n" +
                                " |_|   |_____/_/   \\_\\____|   |_/_/   \\_\\_|\\_\\_____|_| \\_|\n" +
                                "                                                          ");
                        Main.getFrmTest().addGameEvent(new Farcry1GameEvent(new FC1DetailsMessageEvent(this, gameState, starttime, gametimer, timeWhenTheFlagWasActivated, maxgametime, capturetime, pausingSince, resumingSince, lastrespawn, respawninterval, resumeInterval, getRemaining()), new ImageIcon((getClass().getResource("/artwork/rocket32.png")))), 0);
                        addEventToList = true;
                        fireMessage(textMessageList, new MessageEvent(this, gameState, "assault.gamestate.outcome.flag.taken"));
                        break;
                    }
                    case GAME_OUTCOME_FLAG_DEFENDED: {
                        logger.info("\n  _____ _        _    ____   ____  _____ _____ _____ _   _ ____  _____ ____  \n" +
                                " |  ___| |      / \\  / ___| |  _ \\| ____|  ___| ____| \\ | |  _ \\| ____|  _ \\ \n" +
                                " | |_  | |     / _ \\| |  _  | | | |  _| | |_  |  _| |  \\| | | | |  _| | | | |\n" +
                                " |  _| | |___ / ___ \\ |_| | | |_| | |___|  _| | |___| |\\  | |_| | |___| |_| |\n" +
                                " |_|   |_____/_/   \\_\\____| |____/|_____|_|   |_____|_| \\_|____/|_____|____/ \n" +
                                "                                                                             ");
                        Main.getFrmTest().addGameEvent(new Farcry1GameEvent(new FC1DetailsMessageEvent(this, gameState, starttime, gametimer, timeWhenTheFlagWasActivated, maxgametime, capturetime, pausingSince, resumingSince, lastrespawn, respawninterval, resumeInterval, getRemaining()), new ImageIcon((getClass().getResource("/artwork/shield32.png")))), 0);
                        addEventToList = true;
//                        fireMessage(gameTimerList, new FC1DetailsMessageEvent(this, gameState, starttime, gametimer, timeWhenTheFlagWasActivated, maxgametime, capturetime, pausingSince, resumingSince, lastrespawn, respawninterval, resumeInterval, lastRemainingTime));
                        logger.debug("defended gametimer: "+gametimer);
                        fireMessage(textMessageList, new MessageEvent(this, gameState, "assault.gamestate.outcome.flag.defended"));
                        break;
                    }
                    case GAME_GOING_TO_PAUSE: {
                        pausingSince = System.currentTimeMillis();
                        resumeToState = previousGameState; // falls doch kein revert gebraucht wird
                        Main.getPinHandler().pause();
                        fireMessage(textMessageList, new MessageEvent(this, gameState, "assault.gamestate.going.to.pause"));
                        setGameState(GAME_PAUSING);
                        break;
                    }
                    case GAME_PAUSING: {
                        fireMessage(textMessageList, new MessageEvent(this, gameState, "assault.gamestate.paused"));
                        break;
                    }
                    case GAME_GOING_TO_RESUME: {
                        resumingSince = System.currentTimeMillis();
                        fireMessage(textMessageList, new MessageEvent(this, gameState, "assault.gamestate.going.to.resume"));
                        Main.getFrmTest().setToPauseMode(false);
                        break;
                    }
                    case GAME_RESUMED: {

                        // Korrektur der Pausen und Resume Zeit
                        long pause_period = System.currentTimeMillis() - pausingSince;      // damit ist auch die resume zeit inbegriffen.
                        logger.debug("diese Pause (inklusive dem resume) dauerte: " + Tools.formatLongTime(pause_period));

                        resumingSince = -1l;
                        pausingSince = -1l;

                        // wichtig, sonst werden weitere events nach beenden der Pause OHNE Revert hinzugefügt.
                        addEventToList = revertEvent != null;

                        // wenn es einen Event gibt, zu dem Zurückgesprungen werden soll, dann
                        // muss er jetzt berücksichtigt werden.
                        if (revertEvent != null) {
                            logger.debug("\n" +
                                    "  ____  _______     _______ ____ _____   _______     _______ _   _____\n" +
                                    " |  _ \\| ____\\ \\   / / ____|  _ \\_   _| | ____\\ \\   / / ____| \\ | |_   _|\n" +
                                    " | |_) |  _|  \\ \\ / /|  _| | |_) || |   |  _|  \\ \\ / /|  _| |  \\| | | |  \n" +
                                    " |  _ <| |___  \\ V / | |___|  _ < | |   | |___  \\ V / | |___| |\\  | | |  \n" +
                                    " |_| \\_\\_____|  \\_/  |_____|_| \\_\\|_|   |_____|  \\_/  |_____|_| \\_| |_|  \n" +
                                    "                                                                         ");
                            logger.debug(revertEvent.toString());

                            resumeToState = revertEvent.getMessageEvent().getGameState();

                            // die Pause und das Resume muss hier nicht berechnet werden. Durch currentTimeMillis() ist das nicht nötig.
                            // nur wenn die Pause OHNE Revert beendet wird, muss die Starttime entsprechend verschoben werden.
                            starttime = System.currentTimeMillis() - revertEvent.getMessageEvent().getGametimer() - revertEvent.getEventDuration();

                            // das und die starttime reichen aus um die restlichen timer zu errechnen, was dann in run() aus passiert.
                            lastrespawn = revertEvent.getMessageEvent().getLastrespawn();


                            timeWhenTheFlagWasActivated = -1l;
                            if (resumeToState == GAME_FLAG_HOT) {
                                timeWhenTheFlagWasActivated = revertEvent.getMessageEvent().getTimeWhenTheFlagWasActivated();
                                logger.debug("flagactivate ist nun: " + Tools.formatLongTime(timeWhenTheFlagWasActivated));
                            }

                            Main.getFrmTest().setRevertEvent(null);
                            revertEvent = null;
                            Main.getFrmTest().clearEvents(); // jedes revert geht nur einmal, danach ist die Liste leer.
                        } else {
                            // pause_perios enthält auch die Zeit für den Resume
                            starttime = starttime + pause_period;
                        }

                        fireMessage(textMessageList, new MessageEvent(this, gameState, "assault.gamestate.resumed"));
                        Main.getPinHandler().resume();

                        int resume = resumeToState;
                        resumeToState = -1; // das muss hier schon erledigt werden, sonst stolpere ich da beim nächsten Durchlauf drüber.
                        // durch verwendung des LOKALEN resume 

                        setGameState(resume);
                        break;
                    }

                    default: {
                        fireMessage(textMessageList, new MessageEvent(this, gameState, "msg.error"));
                    }
                }

                previousGameState = gameState;
            }
        } finally {
            lock.unlock();
        }
    }


    @Override
    public void prepareGame() {
        previousGameState = GAME_NON_EXISTENT;
        setGameState(GAME_PRE_GAME);
    }

    @Override
    public void startGame() {
        setGameState(GAME_FLAG_ACTIVE);
    }

    @Override
    public void quitGame() {
        thread.interrupt();
    }

    @Override
    public void togglePause() {
        if (isPausing()) {
            setGameState(GAME_GOING_TO_RESUME);
        } else {
            // DEFENDED, weil das könnte ja ein getroffener Verteidiger noch schnell entschärft haben.
            // Wenn er das im OVERTIME macht, dann ist ein UNDO nötig.
            if (isGameRunning() || gameState == GAME_OUTCOME_FLAG_DEFENDED) {
                Main.getFrmTest().setToPauseMode(true);
                setGameState(GAME_GOING_TO_PAUSE);
            }
        }
    }

    public int getGameState() {
        return gameState;
    }


    protected synchronized void fireMessage(EventListenerList listeners, MessageEvent textMessage) {
        for (MessageListener listener : listeners.getListeners(MessageListener.class)) {
            listener.messageReceived(textMessage);
        }
    }


    public long getRemaining() {
        long endtime = maxgametime;
        if (gameState == Farcry1AssaultThread.GAME_FLAG_HOT) {
            endtime = timeWhenTheFlagWasActivated + capturetime;
        }
        return endtime - gametimer;
    }

    public boolean isGameRunning() {
        return gameState == Farcry1AssaultThread.GAME_FLAG_ACTIVE || gameState == Farcry1AssaultThread.GAME_FLAG_HOT || gameState == Farcry1AssaultThread.GAME_FLAG_COLD;
    }

    public boolean isGameJustEnded() {
        return gameState == Farcry1AssaultThread.GAME_OUTCOME_FLAG_TAKEN || gameState == Farcry1AssaultThread.GAME_OUTCOME_FLAG_DEFENDED;
    }


    // Die bewertung der Spielsituation erfolgt in Zyklen.
    public void run() {

        while (!thread.isInterrupted()) {

//            threadcycles++;
            if (isGameRunning()) {
                gametimer = System.currentTimeMillis() - starttime;
//                long respawntimer = lastrespawn + respawninterval - gametimer;
                lastRemainingTime = getRemaining();

//                if (threadcycles % 10 == 0) { // nicht jedes mal die gameTime als event melden. Ist nicht nötig.
                    fireMessage(gameTimerList, new FC1DetailsMessageEvent(this, gameState, starttime, gametimer, timeWhenTheFlagWasActivated, maxgametime, capturetime, pausingSince, resumingSince, lastrespawn, respawninterval, resumeInterval, lastRemainingTime));
                    if (lastrespawn + respawninterval <= gametimer) {
                        lastrespawn = gametimer;
                    }

//                }
            } else if (pausingSince >= 0) {

//                if (threadcycles % 10 == 0) { // nicht jedes mal die gameTime als event melden. Ist nicht nötig.
                    fireMessage(gameTimerList, new FC1DetailsMessageEvent(this, gameState, starttime, gametimer, timeWhenTheFlagWasActivated, maxgametime, capturetime, pausingSince, resumingSince, lastrespawn, respawninterval, resumeInterval, lastRemainingTime));
//                }

            } else if (gameState == GAME_PRE_GAME) {
//                if (threadcycles % 10 == 0) { // nicht jedes mal die gameTime als event melden. Ist nicht nötig.
                    fireMessage(gameTimerList, new FC1DetailsMessageEvent(this, gameState, starttime, gametimer, timeWhenTheFlagWasActivated, maxgametime, capturetime, pausingSince, resumingSince, lastrespawn, respawninterval, resumeInterval, lastRemainingTime));
//                }
            }


            try {

                if (gameState == GAME_FLAG_COLD) {
                    if (getRemaining() <= 0l) {
                        setGameState(GAME_OUTCOME_FLAG_DEFENDED);
                    }
//                    fireMessage(gameTimerList, new FC1DetailsMessageEvent(this, gameState, starttime, gametimer, timeWhenTheFlagWasActivated, maxgametime, capturetime, pausingSince, resumingSince, lastrespawn, respawninterval, resumeInterval, lastRemainingTime));
                }

                if (gameState == GAME_FLAG_HOT) {
                    if (getRemaining() <= 0l) {
                        setGameState(GAME_OUTCOME_FLAG_TAKEN);
                    }
//                    fireMessage(gameTimerList, new FC1DetailsMessageEvent(this, gameState, starttime, gametimer, timeWhenTheFlagWasActivated, maxgametime, capturetime, pausingSince, resumingSince, lastrespawn, respawninterval, resumeInterval, lastRemainingTime));
                }

                if (gameState == GAME_GOING_TO_RESUME) {
                    // um einen Countdown zu zeigen, bevor es weiter geht.
                    // logger.debug("resuming in " + new DateTime(resumeInterval - (System.currentTimeMillis() - resumingSince), DateTimeZone.UTC).toString("mm:ss"));
                    if (System.currentTimeMillis() - resumingSince >= resumeInterval) {
                        setGameState(GAME_RESUMED);
                    } else {
                        fireMessage(gameTimerList, new FC1DetailsMessageEvent(this, gameState, starttime, gametimer, timeWhenTheFlagWasActivated, maxgametime, capturetime, pausingSince, resumingSince, lastrespawn, respawninterval, resumeInterval, lastRemainingTime));
                    }
                }

                Thread.sleep(millispercycle);
            } catch (InterruptedException ie) {
                logger.debug(this + " interrupted!");
            }
        }
    }


    public void setRevertEvent(Farcry1GameEvent revertEvent) {
        this.revertEvent = revertEvent;
    }
}
