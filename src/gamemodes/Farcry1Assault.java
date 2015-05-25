package gamemodes;

import events.MessageEvent;
import events.MessageListener;

import javax.swing.event.EventListenerList;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by tloehr on 25.04.15.
 */
public class Farcry1Assault extends Thread {
    public final Logger LOGGER;
    private BigDecimal cycle;
    private final int seconds2capture;
    private BigDecimal MAXCYCLES;
    private int gameState, previousGameState;
    private long starttime = 0, endtime = 0;

    public static final int GAME_PRE_GAME = 0;
    public static final int GAME_FLAG_ACTIVE = 1;
    public static final int GAME_FLAG_COLD = 2;
    public static final int GAME_FLAG_HOT = 3;
    public static final int GAME_ROCKET_LAUNCHED = 5;
    public static final int GAME_OUTCOME_FLAG_TAKEN = 6;
    public static final int GAME_OUTCOME_FLAG_DEFENDED = 7;

    public static final String[] GAME_MODES = new String[]{"PREGAME","FLAG_ACTIVE","FALG_COLD","FLAG_HOT","ROCKET_LAUNCHED","FLAG_TAKEN","FLAG_DEFENDED"};

    DateFormat formatter = new SimpleDateFormat("mm:ss");

    private final EventListenerList messageList, gameTimerList, percentageList, gameModeList;


    public Farcry1Assault(MessageListener messageListener, MessageListener gameTimerListener, MessageListener percentageListener, MessageListener gameModeListener ,int maxcycles, int seconds2capture) {
        super();

        setName("threads.FarcryAssaultThread");
        messageList = new EventListenerList();
        gameTimerList = new EventListenerList();
        percentageList = new EventListenerList();
        gameModeList = new EventListenerList();


        messageList.add(MessageListener.class, messageListener);
        gameTimerList.add(MessageListener.class, gameTimerListener);
        percentageList.add(MessageListener.class, percentageListener);
        gameModeList.add(MessageListener.class, gameModeListener);

        this.seconds2capture = seconds2capture;

        gameState = GAME_PRE_GAME;
        previousGameState = -1;

        MAXCYCLES = new BigDecimal(maxcycles);
        resetCycle();

        LOGGER = Logger.getLogger(getName());
        LOGGER.setLevel(Level.FINEST);

        LOGGER.info("working");

    }

    public synchronized void setGameState(int state) {
        this.gameState = state;
        if (gameState != previousGameState) {
            previousGameState = gameState;
            fireMessage(gameModeList, new MessageEvent(this, gameState));
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

    public synchronized void toggleFlag() {
        if (gameState == GAME_PRE_GAME || gameState == GAME_OUTCOME_FLAG_TAKEN || gameState == GAME_OUTCOME_FLAG_DEFENDED)
            return;

        if (gameState == GAME_FLAG_ACTIVE) {
            setGameState(GAME_FLAG_COLD);
        }

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
        while (!isInterrupted()) {

            if (gameState == GAME_FLAG_COLD && System.currentTimeMillis() > endtime) {
                setGameState(GAME_OUTCOME_FLAG_DEFENDED);
            }

            String dateFormatted = "00:00:00";
            if (endtime > System.currentTimeMillis()) {
                Date date = new Date(endtime - System.currentTimeMillis());
                System.out.println(endtime - System.currentTimeMillis());
                dateFormatted = formatter.format(date);
            }

            fireMessage(gameTimerList, new MessageEvent(this, dateFormatted));

            try {
                switch (gameState) {
                    case GAME_PRE_GAME: {
                        fireMessage(messageList, new MessageEvent(this, "assault.gamestate.pre.game"));
                        break;
                    }
                    case GAME_FLAG_ACTIVE: {
                        fireMessage(messageList, new MessageEvent(this, "assault.gamestate.flag.is.active"));
                        starttime = System.currentTimeMillis();
                        endtime = starttime + (seconds2capture * 1000);
                        break;
                    }
                    case GAME_FLAG_HOT: {
                        fireMessage(messageList, new MessageEvent(this, "assault.gamestate.flag.is.hot"));
                        if (cycle.compareTo(MAXCYCLES) >= 0) {
                            setGameState(GAME_ROCKET_LAUNCHED);
                        }
                        break;
                    }
                    case GAME_ROCKET_LAUNCHED: {
                        setGameState(GAME_OUTCOME_FLAG_TAKEN);
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

                    default: {
                        fireMessage(messageList, new MessageEvent(this, "msg.error"));
                    }
                }


                Thread.sleep(50); // Milliseconds

            } catch (InterruptedException ie) {

                LOGGER.log(Level.FINE, "FarcryAssaultThread interrupted!");
            }
        }
    }
}
