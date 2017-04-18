package gamemodes;

import interfaces.MessageEvent;
import interfaces.MessageListener;
import main.MissionBox;
import org.apache.log4j.Logger;

import javax.swing.event.EventListenerList;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

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


    private int gameState, previousGameState;


    // makes sure that the time event is only triggered once a second
    private long threadcycles = 0;

    private final long millispercycle = 50;

    private long starttime = 0l; //in millis(), when did the game start ?
    private long gametimer = 0l; //how long is the game running ? starting at 0.
    private long maxgametime = 0l; // wie lange kann dieses Spiel maximal laufen
    private long capturetime = 0l; // wie lange muss die Flagge gehalten werden bis sie erobert wurde ?

    private long pause = 0l;

    public static final int GAME_NON_EXISTENT = -1;
    public static final int GAME_PRE_GAME = 0;
    public static final int GAME_FLAG_ACTIVE = 1;
    public static final int GAME_FLAG_COLD = 2;
    public static final int GAME_FLAG_HOT = 3;
    public static final int GAME_OUTCOME_FLAG_TAKEN = 4;
    public static final int GAME_OUTCOME_FLAG_DEFENDED = 5;

    public static final String[] GAME_MODES = new String[]{"PREGAME", "FLAG_ACTIVE", "FLAG_COLD", "FLAG_HOT", "FLAG_TAKEN", "FLAG_DEFENDED"};

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

        maxgametime = Integer.parseInt(MissionBox.getConfig().getProperty(MissionBox.FCY_GAMETIME)) * 60000l;
        capturetime = Integer.parseInt(MissionBox.getConfig().getProperty(MissionBox.FCY_TIME2CAPTURE)) * 1000l;


        restartGame();
    }


    public boolean isPaused() {
        return pause > 0;
    }


    public void pause() {
        MissionBox.getPinHandler().pause();
        pause = System.currentTimeMillis();
    }

    public void resume() {
        MissionBox.getPinHandler().resume();
        starttime = starttime + pause; // ursprüngliche Startzeit anpassen
        pause = 0l;
    }

    public void setFlagHot(boolean hot) {
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
     * Hier ist die eigentliche Spielmechanik drin. Also was kommt nach was.
     * Durch die Game Event Messages wird das übergeordnete Farcry1Assault verständigt. DORT werden
     * dann die Sirenen und die Leucht-Signale gesetzt.
     *
     * @param state
     */
    private synchronized void setGameState(int state) {
        this.gameState = state;
        logger.debug(GAME_MODES[gameState]);

        if (gameState != previousGameState) {
            previousGameState = gameState;
            fireMessage(gameModeList, new MessageEvent(this, gameState));

            switch (gameState) {
                case GAME_PRE_GAME: {
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
                    MissionBox.getFrmTest().addGameEvent(new Farcry1GameEvent(gameState, gametimer));
                    fireMessage(textMessageList, new MessageEvent(this, gameState, "assault.gamestate.flag.is.hot"));
                    break;
                }
                case GAME_FLAG_COLD: {
                    MissionBox.getFrmTest().addGameEvent(new Farcry1GameEvent(gameState, gametimer));
                    fireMessage(textMessageList, new MessageEvent(this, gameState, "assault.gamestate.flag.is.cold"));
                    break;
                }
                case GAME_OUTCOME_FLAG_TAKEN: {
                    MissionBox.getFrmTest().addGameEvent(new Farcry1GameEvent(gameState, gametimer));
                    fireMessage(textMessageList, new MessageEvent(this, gameState, "assault.gamestate.outcome.flag.taken"));
                    break;
                }
                case GAME_OUTCOME_FLAG_DEFENDED: {
                    MissionBox.getFrmTest().addGameEvent(new Farcry1GameEvent(gameState, gametimer));
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
    public void restartGame() {
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

    public int getGameState() {
        return gameState;
    }


    protected synchronized void fireMessage(EventListenerList listeners, MessageEvent textMessage) {
        for (MessageListener listener : listeners.getListeners(MessageListener.class)) {
            listener.messageReceived(textMessage);
        }
    }

    public boolean timeIsUp() {
        long endtime = maxgametime;

        if (gameState == GAME_FLAG_HOT) {
            Farcry1GameEvent lastEvent = MissionBox.getFrmTest().getLastEvent();
            endtime = lastEvent.getGametimer() + capturetime;
        }

        return gametimer >= endtime;
    }

    public boolean isGameRunning() {
        return gameState == Farcry1AssaultThread.GAME_FLAG_ACTIVE || gameState == Farcry1AssaultThread.GAME_FLAG_HOT || gameState == Farcry1AssaultThread.GAME_FLAG_COLD;
    }


    // Die bewertung der Spielsituation erfolgt in Zyklen.
    public void run() {

        while (!thread.isInterrupted() && pause == 0l) {

            if (isGameRunning()) {

                gametimer = System.currentTimeMillis() - starttime;
//                logger.debug(gametimer);

                threadcycles++;

                if (threadcycles % 15 == 0) { // nicht jedes mal die gameTime als event melden. Ist nicht nötig.
                    Farcry1GameEvent lastEvent = MissionBox.getFrmTest().getLastEvent();
                    fireMessage(gameTimerList, new MessageEvent(this, gameState, lastEvent.getEndtime(maxgametime, capturetime) - gametimer)); // verbleibende Zeit
                }

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
                        setGameState(GAME_OUTCOME_FLAG_TAKEN);
                    }

                    Farcry1GameEvent lastEvent = MissionBox.getFrmTest().getLastEvent();
                    long flagactivation = lastEvent.getGametimer();
                    long rocketWillLaunch = flagactivation + capturetime;


                    BigDecimal progress = new BigDecimal(gametimer - flagactivation).divide(new BigDecimal(rocketWillLaunch - flagactivation), 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));



                    fireMessage(percentageList, new MessageEvent(this, gameState, progress));
                }

//                if (gameState == GAME_OUTCOME_FLAG_TAKEN || gameState == GAME_OUTCOME_FLAG_DEFENDED) {
//                    setGameState(GAME_OVER);
//                }

                Thread.sleep(millispercycle);
            } catch (InterruptedException ie) {
                logger.debug(this + " interrupted!");
            }
        }
    }
}
