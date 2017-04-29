package gamemodes;

import interfaces.MessageEvent;
import interfaces.MessageListener;
import main.MissionBox;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.math.BigDecimal;

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


    private int gameState, previousGameState, resumeToState = -1;
    private final long millisBeforeResuming = 5000;

    // makes sure that the time event is only triggered once a second
    private long threadcycles = 0;

    private final long millispercycle = 50;

    /**
     * systemzeit des starts. wird nur gebraucht um den gametimer zu setzen. das er bei 0 anfängt muss er relativ zur Startzeit berechnet werden.
     */
    private long starttime = -1l;


    private long gametimer = 0l; // wie lange läuft das Spiel schon ?
    private long timeWhenTheFlagWasActivated = -1l; // wann wurde die Flagge zuletzt aktiviert. -1l heisst, nicht aktiv.
    private long maxgametime = 0l; // wie lange kann dieses Spiel maximal laufen
    private long capturetime = 0l; // wie lange muss die Flagge gehalten werden bis sie erobert wurde ?

    private long pausingSince = -1l;
    private long resumingSince = -1l;

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

    public static final String[] GAME_MODES = new String[]{"PREPARE_GAME", "FLAG_ACTIVE", "FLAG_COLD", "FLAG_HOT", "FLAG_TAKEN", "FLAG_DEFENDED", "GOING_TO_PAUSE", "PAUSING", "GOING_TO_RESUME", "GAME_RESUMED"};


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
    private final EventListenerList percentageList;
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


    /**
     * @param messageListener
     * @param gameTimerListener
     * @param percentageListener
     * @param gameModeListener
     * @param maxgametime        - in Minuten
     * @param capturetime        - in Sekunden
     */

    public Farcry1AssaultThread(MessageListener messageListener, MessageListener gameTimerListener, MessageListener percentageListener, MessageListener gameModeListener, long maxgametime, long capturetime) {
        super();
        setMaxgametime(maxgametime);
        setCapturetime(capturetime);
        thread = new Thread(this);
        logger.setLevel(MissionBox.getLogLevel());

        textMessageList = new EventListenerList();
        gameTimerList = new EventListenerList();
        percentageList = new EventListenerList();
        gameModeList = new EventListenerList();

        textMessageList.add(MessageListener.class, messageListener);
        gameTimerList.add(MessageListener.class, gameTimerListener);
        percentageList.add(MessageListener.class, percentageListener);
        gameModeList.add(MessageListener.class, gameModeListener);

        previousGameState = GAME_NON_EXISTENT;

        prepareGame();
    }

    public boolean isPausing() {
        return pausingSince > -1l;
    }

    public void setFlagHot(boolean hot) {
        if (isPausing()) return; // eigentlich brauche ich diese Abfrage nicht, weil dass durch die Konstruktion schon
        // bewältigt wird. Aber so ist es schön einheitlich.
        if (hot) {
            if (gameState == GAME_FLAG_COLD) {
                setGameState(GAME_FLAG_HOT);
            }
        } else {
            if (gameState == GAME_FLAG_HOT) {
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
    private synchronized void setGameState(int state) {
        this.gameState = state;
        logger.debug(GAME_MODES[gameState]);

        if (gameState != previousGameState) {

            previousGameState = gameState;  // das dient nur dazu, damit ich merken kann, wenn sich wirklich ein Zustand geändert hat.
            // welcher genau das vorher war kümmert mich nicht. Auch bei einem REVERT setze ich diese Variable einfach auf IRGENDWAS, dass
            // garantiert anders ist als gameState. z.B. -1l
            fireMessage(gameModeList, new MessageEvent(this, gameState));

            switch (gameState) {
                case GAME_PRE_GAME: {
                    starttime = -1l;
                    MissionBox.shutdownEverything();
                    fireMessage(textMessageList, new MessageEvent(this, gameState, "assault.gamestate.pre.game"));
                    MissionBox.getFrmTest().clearEvents();
                    break;
                }
                case GAME_FLAG_ACTIVE: { // hier wird das Spiel gestartet
                    gametimer = 0l;
                    starttime = System.currentTimeMillis();
                    fireMessage(textMessageList, new MessageEvent(this, gameState, "assault.gamestate.flag.is.active"));
                    setGameState(GAME_FLAG_COLD);
                    break;
                }
                case GAME_FLAG_HOT: {
                    MissionBox.getFrmTest().addGameEvent(new Farcry1GameEvent(gameState, gametimer, maxgametime, capturetime, new ImageIcon((getClass().getResource("/artwork/ledred32.png")))));
                    if (timeWhenTheFlagWasActivated == -1l) { // dadurch verhindere ich, dass bei einem revert dieser Wert falsch gesetzt wird.
                        timeWhenTheFlagWasActivated = gametimer; // also jetzt.
                    }
                    fireMessage(textMessageList, new MessageEvent(this, gameState, "assault.gamestate.flag.is.hot"));
                    break;
                }
                case GAME_FLAG_COLD: {
                    MissionBox.getFrmTest().addGameEvent(new Farcry1GameEvent(gameState, gametimer, maxgametime, capturetime, new ImageIcon((getClass().getResource("/artwork/ledgreen32.png")))));
                    timeWhenTheFlagWasActivated = -1l;
                    fireMessage(textMessageList, new MessageEvent(this, gameState, "assault.gamestate.flag.is.cold"));
                    break;
                }
                case GAME_OUTCOME_FLAG_TAKEN: {
                    MissionBox.getFrmTest().addGameEvent(new Farcry1GameEvent(gameState, gametimer, maxgametime, capturetime, new ImageIcon((getClass().getResource("/artwork/rocket32.png")))));
                    fireMessage(textMessageList, new MessageEvent(this, gameState, "assault.gamestate.outcome.flag.taken"));
                    break;
                }
                case GAME_GOING_TO_PAUSE: {
                    pausingSince = System.currentTimeMillis();
                    MissionBox.getPinHandler().pause();
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

//                    MissionBox.getFrmTest().setRevertEvent();
                    // der Ablauf der Resume Zeit wird durch RUN() bemerkt und der Gamemode entsprechend gesetzt.
                    // setGameState(GAME_RESUMED);
                    break;
                }
                case GAME_RESUMED: {
                    resumingSince = -1l;
                    // die Startzeit muss um den Zeitraum für die Pause verschoben werden.
                    long pause_period = System.currentTimeMillis() - pausingSince;

                    // wenn es einen Event gibt, zu dem Zurückgesprungen werden soll, dann
                    // muss er jetzt berücksichtigt werden.
                    if (revertEvent != null) {
                        resumeToState = revertEvent.getGameState();
                        starttime = starttime + revertEvent.getGametimerAtEnd() + 1; // Verschiebt die Startzeit um die Dauer dieses Events.

                        if (resumeToState == GAME_FLAG_HOT) {
                            timeWhenTheFlagWasActivated = revertEvent.getGametimerAtStart();
                        } else {
                            timeWhenTheFlagWasActivated = -1l;
                        }

                        revertEvent = null;
                        MissionBox.getPinHandler().clear(); // werden gleich neu gesetzt. daher alles löschen.
                        MissionBox.getFrmTest().setRevertEvent(null);
                        MissionBox.getFrmTest().clearEvents(); // jedes revert geht nur einmal, danach ist die Liste leer.
                        // es macht keinen Sinn den gametimer zu verändern. Der wird sowieso in der RUN() Schleife berechnet.
                    }

                    starttime = starttime + pause_period; // Jetzt noch die Anpassung der Startzeit um die Länge dieser Pause.
                    gametimer = System.currentTimeMillis() - starttime; // das ist eigentlich redundant. Aber dann kommt es nicht zu kurzzeitigen falschen Anzeigen auf der Uhr.
                    pausingSince = -1l;

                    fireMessage(textMessageList, new MessageEvent(this, gameState, "assault.gamestate.resumed"));
                    MissionBox.getPinHandler().resume();
                    MissionBox.getFrmTest().setToPauseMode(false);
                    setGameState(resumeToState);
                    break;
                }
                case GAME_OUTCOME_FLAG_DEFENDED: {
                    MissionBox.getFrmTest().addGameEvent(new Farcry1GameEvent(gameState, gametimer, maxgametime, capturetime, new ImageIcon((getClass().getResource("/artwork/shield32.png")))));
                    fireMessage(textMessageList, new MessageEvent(this, gameState, "assault.gamestate.outcome.flag.defended"));
                    break;
                }
                default: {
                    fireMessage(textMessageList, new MessageEvent(this, gameState, "msg.error"));
                }
            }
        }
    }


    @Override
    public void prepareGame() {
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
        if (!isPausing()) {
            if (isGameRunning() || isGameJustEnded()) {
                MissionBox.getFrmTest().setToPauseMode(true);
                resumeToState = previousGameState; // merken für später, falls doch kein REVERT gemacht wird.
                setGameState(GAME_GOING_TO_PAUSE);
            }
        } else {
            setGameState(GAME_GOING_TO_RESUME);
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


    private long getEstimatedEndOfGame() {
        long endtime = maxgametime;
        if (gameState == GAME_FLAG_HOT) {
            endtime = timeWhenTheFlagWasActivated + capturetime;
        }
        return endtime;
    }

    private boolean timeIsUp() {
        return gametimer >= getEstimatedEndOfGame();
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

            if (isGameRunning()) {

                gametimer = System.currentTimeMillis() - starttime;

                threadcycles++;

                if (threadcycles % 15 == 0) { // nicht jedes mal die gameTime als event melden. Ist nicht nötig.
                    fireMessage(gameTimerList, new MessageEvent(this, gameState, getEstimatedEndOfGame() - gametimer, gameState == GAME_FLAG_HOT ? maxgametime - gametimer : null)); // verbleibende Zeit
                }
            }

            if (pausingSince >= 0) {

                threadcycles++;

                if (threadcycles % 15 == 0) { // nicht jedes mal die gameTime als event melden. Ist nicht nötig.
                    fireMessage(gameTimerList, new MessageEvent(this, gameState, System.currentTimeMillis() - pausingSince, null)); // verbleibende Zeit
                }

            }


            try {

                if (gameState == GAME_FLAG_COLD) {
                    if (timeIsUp()) {
                        setGameState(GAME_OUTCOME_FLAG_DEFENDED);
                    }
                    fireMessage(percentageList, new MessageEvent(this, gameState, new BigDecimal(-1)));
                }

                if (gameState == GAME_FLAG_HOT) {
                    if (timeIsUp()) {
                        setGameState(GAME_OUTCOME_FLAG_TAKEN);
                    }

                    long flagwillbetaken = timeWhenTheFlagWasActivated + capturetime;
                    BigDecimal progress = new BigDecimal(gametimer - timeWhenTheFlagWasActivated).divide(new BigDecimal(flagwillbetaken - timeWhenTheFlagWasActivated), 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));

                    fireMessage(percentageList, new MessageEvent(this, gameState, progress));
                }

                if (gameState == GAME_GOING_TO_RESUME) {
                    // um einen Countdown zu zeigen, bevor es weiter geht.
                    // logger.debug("resuming in " + new DateTime(millisBeforeResuming - (System.currentTimeMillis() - resumingSince), DateTimeZone.UTC).toString("mm:ss"));
                    if (System.currentTimeMillis() - resumingSince >= millisBeforeResuming) {
                        setGameState(GAME_RESUMED);
                    } else {
                        fireMessage(gameTimerList, new MessageEvent(this, gameState, millisBeforeResuming - System.currentTimeMillis() + resumingSince, maxgametime)); // verbleibende Zeit
                    }
                }

                Thread.sleep(millispercycle);
            } catch (InterruptedException ie) {
                logger.debug(this + " interrupted!");
            }
        }
    }

//    long getResumingSince(){
//        long l;
//        final ReentrantLock lock;
//    }

    public void setRevertEvent(Farcry1GameEvent revertEvent) {
        this.revertEvent = revertEvent;
    }
}
