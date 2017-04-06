package gamemodes;

import interfaces.MessageEvent;
import interfaces.MessageListener;
import main.MissionBox;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import javax.swing.event.EventListenerList;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Dieser Thread läuft wie ein Motor in Zyklen ab. Jeder Zyklus dauert <b>millispercycle</b> millisekunden.
 * Nach <b>maxcycles</b> Zyklen ist das Spiel vorbei. <b>TIME2CAP</b> ist die Anzahl der Sekunden, die es braucht, bis die Flagge genommen wurde. (Wenn sie vorher nicht deaktiviert wurde).
 * <p>
 * Farcry1Assault
 * Farcry1AssaultThread
 * setGameState(state) – setzt die Spielmechanik. Also was passiert bei einem bestimmten GameMode
 * run() - Das ist der innere Auswertungszyklus. Eine Art Motor, der bei jedem Durchgang die Situation bewertet und entsprechende Änderungen am Zustand der Box vornimmt.
 */
public class Farcry1AssaultThread implements Runnable, GameThreads {
    final Logger logger = Logger.getLogger(getClass());
    private final Thread thread;
    private int GAMETIMEINSECONDS;
    private int TIME2CAP;


//    private BigDecimal cycle;

    //    private BigDecimal MAXCYCLES;
    private int gameState, previousGameState;
    private int afterglow = 0, agseconds = 20, rocketseconds = 9;
    private DateTime starttime, flagactivation, endtime, rockettime;
    // makes sure that the time event is only triggered once a second
    private long threadcycles = 0;
    private final long millispercycle = 50;
    private DateTime pausedSince = null;

//    ArrayList<Farcry1GameEvent> eventList = null;

    public static final int GAME_NON_EXISTENT = -1;
    public static final int GAME_PRE_GAME = 0;
    public static final int GAME_FLAG_ACTIVE = 1;
    public static final int GAME_FLAG_COLD = 2;
    public static final int GAME_FLAG_HOT = 3;
    public static final int GAME_ROCKET_LAUNCHED = 4;
    public static final int GAME_OUTCOME_FLAG_TAKEN = 5;
    public static final int GAME_OUTCOME_FLAG_DEFENDED = 6;
    public static final int GAME_OVER = 7;
    public static final int GAME_PAUSED = 8;
    public static final int GAME_RESUME = 9;

    public static final String[] GAME_MODES = new String[]{"PREGAME", "FLAG_ACTIVE", "FLAG_COLD", "FLAG_HOT", "ROCKET", "FLAG_TAKEN", "FLAG_DEFENDED", "GAME_OVER", "GAME_PAUSE", "GAME_RESUME"};

    DateFormat formatter = new SimpleDateFormat("mm:ss");

    private final EventListenerList textMessageList, gameTimerList, percentageList, gameModeList;

    public Farcry1AssaultThread(MessageListener messageListener, MessageListener gameTimerListener, MessageListener percentageListener, MessageListener gameModeListener) {
        super();
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

        restartGame();
    }


    public void pause() {
        setGameState(GAME_PAUSED);
    }

    public void resume() {
        setGameState(GAME_RESUME);
    }


    /**
     * Hier ist die eingentliche Spielmechanik drin. Also was kommt nach was.
     *
     * @param state
     */
    public synchronized void setGameState(int state) {
//        Farcry1GameEvent undo = null;
//        if (state == -1) { // means UNDO
//            if (eventList.size() != 2) return; // need 2 states to pause
//
//            logger.debug(eventList.toString());
//            // in case of pause, we use the first one, and remove the second.
//            pause = eventList.get(0);
//            eventList.remove(eventList.get(1));
//
//            logger.debug("Reverting back to the following state");
//            logger.debug(pause);
//            MissionBox.log("Reverting back to the following state");
//            MissionBox.log(pause.toString());
//
//            this.gameState = pause.getGameState();
//        } else {
//
//        }
        this.gameState = state;

        if (gameState != previousGameState) {
            previousGameState = gameState;
            fireMessage(gameModeList, new MessageEvent(this, gameState));

            switch (gameState) {
                case GAME_PRE_GAME: {
                    MissionBox.shutdownEverything();
                    fireMessage(textMessageList, new MessageEvent(this, gameState, "assault.gamestate.pre.game"));
                    MissionBox.getFrmTest().clear();
                    break;
                }
                case GAME_FLAG_ACTIVE: {
                    this.GAMETIMEINSECONDS = Integer.parseInt(MissionBox.getConfig().getProperty(MissionBox.FCY_GAMETIME)) * 60;
                    this.TIME2CAP = Integer.parseInt(MissionBox.getConfig().getProperty(MissionBox.FCY_TIME2CAPTURE));
                    fireMessage(textMessageList, new MessageEvent(this, gameState, "assault.gamestate.flag.is.active"));
                    starttime = new DateTime();
                    endtime = starttime.plusSeconds(GAMETIMEINSECONDS);
                    setGameState(GAME_FLAG_COLD);
                    break;
                }
                case GAME_RESUME: {
                    MissionBox.getPinHandler().resume();
                    pausedSince = null;
                    break;
                }
                case GAME_PAUSED: {
                    MissionBox.getPinHandler().pause();
                    pausedSince = new DateTime();

                    this.GAMETIMEINSECONDS = Integer.parseInt(MissionBox.getConfig().getProperty(MissionBox.FCY_GAMETIME)) * 60;
                    this.TIME2CAP = Integer.parseInt(MissionBox.getConfig().getProperty(MissionBox.FCY_TIME2CAPTURE));
                    fireMessage(textMessageList, new MessageEvent(this, gameState, "assault.gamestate.flag.is.active"));
                    starttime = new DateTime();
                    endtime = starttime.plusSeconds(GAMETIMEINSECONDS);
                    setGameState(GAME_FLAG_COLD);
                    break;
                }
                case GAME_FLAG_HOT: {
                    MissionBox.getFrmTest().addGameEvent(new Farcry1GameEvent(state));

                    flagactivation = new DateTime();

                    endtime = flagactivation.plusSeconds(TIME2CAP);

                    logger.debug("Starttime: " + starttime);
                    logger.debug("Flagactivation: " + flagactivation);
                    logger.debug("Endtime: " + endtime);

                    fireMessage(textMessageList, new MessageEvent(this, gameState, "assault.gamestate.flag.is.hot"));
                    break;
                }
                case GAME_ROCKET_LAUNCHED: {
                    fireMessage(textMessageList, new MessageEvent(this, gameState, "assault.gamestate.rocket.launched"));
                    endtime = new DateTime();
                    rockettime = endtime.plusSeconds(rocketseconds); // hier wird noch kurz gewartet bis der raketensound abgespielt ist. danach wird gehts
                    break;
                }
                case GAME_FLAG_COLD: {
                    MissionBox.getFrmTest().addGameEvent(new Farcry1GameEvent(state));

                    flagactivation = new DateTime(0);
                    endtime = starttime.plusSeconds(GAMETIMEINSECONDS);

                    fireMessage(textMessageList, new MessageEvent(this, gameState, "assault.gamestate.flag.is.cold"));
                    break;
                }
                case GAME_OUTCOME_FLAG_TAKEN: {
                    fireMessage(textMessageList, new MessageEvent(this, gameState, "assault.gamestate.outcome.flag.taken"));
                    break;
                }
                case GAME_OUTCOME_FLAG_DEFENDED: {
                    fireMessage(textMessageList, new MessageEvent(this, gameState, "assault.gamestate.outcome.flag.defended"));
                    break;
                }
                case GAME_OVER: {
                    fireMessage(textMessageList, new MessageEvent(this, gameState, "assault.gamestate.after.game"));
                    break;
                }
                default: {
                    fireMessage(textMessageList, new MessageEvent(this, gameState, "msg.error"));
                }
            }
        }
    }


    @Override
    public void restartGame() {
        setGameState(GAME_PRE_GAME);
    }

    @Override
    public void startGame() {
        setGameState(GAME_FLAG_ACTIVE);
    }

    @Override
    public void quitGame() {
        setGameState(GAME_OVER);
        thread.interrupt();
    }

    public int getGameState() {
        return gameState;
    }

    public synchronized boolean isFlagHot() {
        return gameState == GAME_FLAG_HOT;
    }

    public synchronized void setFlag(boolean on) {
//        if (gameState == GAME_PRE_GAME || gameState == GAME_OUTCOME_FLAG_TAKEN || gameState == GAME_OUTCOME_FLAG_TAKEN || gameState == GAME_OUTCOME_FLAG_DEFENDED || gameState == GAME_OVER)
//            return;

        if (gameState != GAME_FLAG_HOT && gameState != GAME_FLAG_COLD) return;
        setGameState(on ? GAME_FLAG_HOT : GAME_FLAG_COLD);
    }

    protected synchronized void fireMessage(EventListenerList listeners, MessageEvent textMessage) {
        for (MessageListener listener : listeners.getListeners(MessageListener.class)) {
            listener.messageReceived(textMessage);
        }
    }

    public boolean timeIsUp() {
        return endtime != null && endtime.isBeforeNow();
    }


    // Die bewertung der Spielsituation erfolgt in Zyklen.
    public void run() {
        while (!thread.isInterrupted()) {

            threadcycles++;

            if (threadcycles % 15 == 0) { // nicht jedes mal die gameTime bestimmen. Ist nicht nötig.
                DateTime gameTime = new LocalDate().toDateTimeAtStartOfDay();
                if (endtime != null && endtime.isAfterNow()) {
                    gameTime = endtime.minus(new DateTime().getMillis());
                }
                fireMessage(gameTimerList, new MessageEvent(this, gameState, gameTime));
            }

            try {

                // bei Pause wird keiner der einzelnen Bedingungen erfüllt.

                if (gameState == GAME_FLAG_COLD) {
                    if (timeIsUp()) {
                        setGameState(GAME_OUTCOME_FLAG_DEFENDED);
                    }
                    fireMessage(percentageList, new MessageEvent(this, gameState, new BigDecimal(-1)));
                }

                if (gameState == GAME_FLAG_HOT) {
                    if (timeIsUp()) {
                        setGameState(GAME_ROCKET_LAUNCHED);
                    }

                    BigDecimal progress = new BigDecimal(new DateTime().getMillis() - flagactivation.getMillis()).divide(new BigDecimal(endtime.getMillis() - flagactivation.getMillis()), 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
                    fireMessage(percentageList, new MessageEvent(this, gameState, progress));
                }

                if (gameState == GAME_ROCKET_LAUNCHED) {
                    // hier wird auf FlagTaken umgesschaltet, sobald die Rakete abgespielt wurde
                    if (rockettime.isBeforeNow()) {
                        setGameState(GAME_OUTCOME_FLAG_TAKEN);
                    }
                }

                if (gameState == GAME_OUTCOME_FLAG_TAKEN || gameState == GAME_OUTCOME_FLAG_DEFENDED) {
                    setGameState(GAME_OVER);
                }

                Thread.sleep(millispercycle);
            } catch (InterruptedException ie) {
                logger.debug(this + " interrupted!");
            }
        }
    }
}
