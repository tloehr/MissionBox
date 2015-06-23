package gamemodes;

import interfaces.MessageEvent;
import interfaces.MessageListener;
import main.MissionBox;
import org.apache.log4j.Logger;

import javax.swing.event.EventListenerList;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by tloehr on 25.04.15.
 */
public class Farcry1AssaultThread implements Runnable, GameThreads {
    final Logger LOGGER = Logger.getLogger(getClass());
    private final Thread thread;
    private final int millispercycle;
    private final int cycledivider;
    private BigDecimal cycle;
    private final int seconds2capture;
    private BigDecimal MAXCYCLES;
    private int gameState, previousGameState;
    private long starttime = 0, endtime = 0, afterglow = 0, agseconds = 20, rockettime = 0, rocketseconds = 7;
    private long threadcycles = 0; // makes sure that the time event is only triggered once a second


    public static final int GAME_NON_EXISTENT = -1;
    public static final int GAME_PRE_GAME = 0;
    public static final int GAME_FLAG_ACTIVE = 1;
    public static final int GAME_FLAG_COLD = 2;
    public static final int GAME_FLAG_HOT = 3;
    public static final int GAME_ROCKET_LAUNCHED = 4;
    public static final int GAME_OUTCOME_FLAG_TAKEN = 5;
    public static final int GAME_OUTCOME_FLAG_DEFENDED = 6;
    public static final int GAME_OVER = 7;

    public static final String[] GAME_MODES = new String[]{"PREGAME", "FLAG_ACTIVE", "FLAG_COLD", "FLAG_HOT", "ROCKET_LAUNCHED", "FLAG_TAKEN", "FLAG_DEFENDED", "GAME_OVER"};

    DateFormat formatter = new SimpleDateFormat("mm:ss");

    private final EventListenerList messageList, gameTimerList, percentageList, gameModeList;


    public Farcry1AssaultThread(MessageListener messageListener, MessageListener gameTimerListener, MessageListener percentageListener, MessageListener gameModeListener, int maxcycles, int seconds2capture, int millispercycle) {
        super();
        this.millispercycle = millispercycle;
        cycledivider = 1000 / millispercycle;
        thread = new Thread(this);



        messageList = new EventListenerList();
        gameTimerList = new EventListenerList();
        percentageList = new EventListenerList();
        gameModeList = new EventListenerList();

        messageList.add(MessageListener.class, messageListener);
        gameTimerList.add(MessageListener.class, gameTimerListener);
        percentageList.add(MessageListener.class, percentageListener);
        gameModeList.add(MessageListener.class, gameModeListener);

        this.seconds2capture = seconds2capture;

        previousGameState = GAME_NON_EXISTENT;

        MAXCYCLES = new BigDecimal(maxcycles);
        resetCycle();

        setGameState(GAME_PRE_GAME);

    }

    public synchronized void setGameState(int state) {
        this.gameState = state;
        if (gameState != previousGameState) {
            previousGameState = gameState;
            fireMessage(gameModeList, new MessageEvent(this, gameState));

            switch (gameState) {
                case GAME_PRE_GAME: {
                    fireMessage(messageList, new MessageEvent(this, "assault.gamestate.pre.game"));
                    endtime = 0;
                    break;
                }
                case GAME_FLAG_ACTIVE: {
                    fireMessage(messageList, new MessageEvent(this, "assault.gamestate.flag.is.active"));
                    starttime = System.currentTimeMillis();
                    endtime = starttime + (seconds2capture * 1000);
                    setGameState(GAME_FLAG_COLD);
                    break;
                }
                case GAME_FLAG_HOT: {
                    fireMessage(messageList, new MessageEvent(this, "assault.gamestate.flag.is.hot"));
                    break;
                }
                case GAME_ROCKET_LAUNCHED: {
                    fireMessage(messageList, new MessageEvent(this, "assault.gamestate.outcome.flag.defended"));
                    endtime = System.currentTimeMillis();
                    rockettime = endtime + (rocketseconds * 1000);
                    afterglow = rockettime + (agseconds * 1000);
                    break;
                }
                case GAME_FLAG_COLD: {
                    fireMessage(messageList, new MessageEvent(this, "assault.gamestate.flag.is.cold"));
                    resetCycle();
                    break;
                }
                case GAME_OUTCOME_FLAG_TAKEN: {
                    fireMessage(messageList, new MessageEvent(this, "assault.gamestate.outcome.flag.taken"));
                    break;
                }
                case GAME_OUTCOME_FLAG_DEFENDED: {
                    fireMessage(messageList, new MessageEvent(this, "assault.gamestate.outcome.flag.defended"));
                    break;
                }
                case GAME_OVER: {
                    fireMessage(messageList, new MessageEvent(this, "assault.gamestate.after.game"));
                    break;
                }
                default: {
                    fireMessage(messageList, new MessageEvent(this, "msg.error"));
                }
            }
        }
    }

    private synchronized void resetCycle() {
        setCycle(BigDecimal.ZERO);
    }

    private synchronized void increaseCycle() {
        setCycle(cycle.add(BigDecimal.ONE));
    }

    private synchronized void setCycle(BigDecimal cycle) {
        this.cycle = cycle;
        BigDecimal progress = cycle.divide(MAXCYCLES, 2, RoundingMode.HALF_UP).multiply(new BigDecimal(100));
        fireMessage(percentageList, new MessageEvent(this, progress));
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


    //    public synchronized void stopGame() {
//        setGameState(GAME_PRE_GAME);
//    }

    public synchronized void toggleFlag() {
        if (gameState == GAME_PRE_GAME || gameState == GAME_OUTCOME_FLAG_TAKEN || gameState == GAME_OUTCOME_FLAG_DEFENDED || gameState == GAME_OVER)
            return;

        if (gameState == GAME_FLAG_COLD) {
            setGameState(GAME_FLAG_HOT);
        } else {
            setGameState(GAME_FLAG_COLD);
        }
    }

    protected synchronized void fireMessage(EventListenerList listeners, MessageEvent textMessage) {
        for (MessageListener listener : listeners.getListeners(MessageListener.class)) {
            listener.messageReceived(textMessage);
        }
    }

    public void run() {
        while (!thread.isInterrupted()) {

            threadcycles++;


            if (threadcycles % cycledivider == 0) {
                String dateFormatted = "00:00";
                if (endtime > System.currentTimeMillis()) {
                    Date date = new Date(endtime - System.currentTimeMillis());
                    dateFormatted = formatter.format(date);
                }

                fireMessage(gameTimerList, new MessageEvent(this, dateFormatted));
            }

            try {

                if (cycle.compareTo(MAXCYCLES) >= 0) {
                    if (gameState == GAME_FLAG_HOT) {
                        setGameState(GAME_ROCKET_LAUNCHED);
                    }
                }

                if (gameState == GAME_FLAG_COLD) {
                    if (endtime < System.currentTimeMillis()) {
                        setGameState(GAME_OUTCOME_FLAG_DEFENDED);
                    }
                }

                if (gameState == GAME_FLAG_HOT) {
                    increaseCycle();
                }

                if (gameState == GAME_ROCKET_LAUNCHED) {
                    if (rockettime < System.currentTimeMillis()) {
                        setGameState(GAME_OUTCOME_FLAG_TAKEN);
                    }
                }

                if (gameState == GAME_OUTCOME_FLAG_TAKEN || gameState == GAME_OUTCOME_FLAG_DEFENDED) {
                    if (afterglow < System.currentTimeMillis()) {
                        setGameState(GAME_OVER);
                    }
                }

                Thread.sleep(millispercycle);
            } catch (InterruptedException ie) {
                LOGGER.debug(this + " interrupted!");
            }
        }
    }
}
