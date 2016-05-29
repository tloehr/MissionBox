package gamemodes;

import interfaces.MessageEvent;
import interfaces.MessageListener;
import main.MissionBox;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import javax.swing.event.EventListenerList;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Dieser Thread läuft wie ein Motor in Zyklen ab. Jeder Zyklus dauert <b>millispercycle</b> millisekunden.
 * Nach <b>maxcycles</b> Zyklen ist das Spiel vorbei. <b>TIME2CAP</b> ist die Anzahl der Sekunden, die es braucht, bis die Flagge genommen wurde. (Wenn sie vorher nicht deaktiviert wurde).
 */
public class Farcry1AssaultThread implements Runnable, GameThreads {
    final Logger LOGGER = Logger.getLogger(getClass());
    private final Thread thread;
    private int GAMETIMEINSECONDS;
    private int TIME2CAP;

//    private BigDecimal cycle;

    //    private BigDecimal MAXCYCLES;
    private int gameState, previousGameState;
    private int afterglow = 0, agseconds = 20, rocketseconds = 7;
    private DateTime starttime, flagactivation, endtime, rockettime;
    // makes sure that the time event is only triggered once a second
    private long threadcycles = 0;
    private final long millispercycle = 50;
    private final long cycledivider;


    public static final int GAME_NON_EXISTENT = -1;
    public static final int GAME_PRE_GAME = 0;
    public static final int GAME_FLAG_ACTIVE = 1;
    public static final int GAME_FLAG_COLD = 2;
    public static final int GAME_FLAG_HOT = 3;
    public static final int GAME_ROCKET_LAUNCHED = 4;
    public static final int GAME_OUTCOME_FLAG_TAKEN = 5;
    public static final int GAME_OUTCOME_FLAG_DEFENDED = 6;
    public static final int GAME_OVER = 7;

    public static final String[] GAME_MODES = new String[]{"PREGAME", "FLAG_ACTIVE", "FLAG_COLD", "FLAG_HOT", "ROCKET", "FLAG_TAKEN", "FLAG_DEFENDED", "GAME_OVER"};

    DateFormat formatter = new SimpleDateFormat("mm:ss");

    private final EventListenerList textMessageList, gameTimerList, percentageList, gameModeList;

    public Farcry1AssaultThread(MessageListener messageListener, MessageListener gameTimerListener, MessageListener percentageListener, MessageListener gameModeListener) {
        super();


        // der cycledivider wird nur benutzt, damit nicht so oft die Zeitausgaben erfolgen.
        cycledivider = 1000 / millispercycle;

        thread = new Thread(this);


        LOGGER.setLevel(MissionBox.getLogLevel());

        textMessageList = new EventListenerList();
        gameTimerList = new EventListenerList();
        percentageList = new EventListenerList();
        gameModeList = new EventListenerList();

        textMessageList.add(MessageListener.class, messageListener);
        gameTimerList.add(MessageListener.class, gameTimerListener);
        percentageList.add(MessageListener.class, percentageListener);
        gameModeList.add(MessageListener.class, gameModeListener);

        previousGameState = GAME_NON_EXISTENT;

        // die Maximalen-Zyklen für einen Spieldurchlauf ergeben sich aus der maximalen Anzahl der Minuten für ein Spiel.
//        MAXCYCLES = new BigDecimal(GAMETIMEINSECONDS).multiply(new BigDecimal(60)).multiply(new BigDecimal(1000)).divide(millispercycle, BigDecimal.ROUND_HALF_UP);
//        resetCycle();

        restartGame();

    }

    /**
     * Hier ist die eingentliche Spielmechanik drin. Also was kommt nach was.
     *
     * @param state
     */
    public synchronized void setGameState(int state) {
        this.gameState = state;
        if (gameState != previousGameState) {
            previousGameState = gameState;
            fireMessage(gameModeList, new MessageEvent(this, gameState));

            switch (gameState) {
                case GAME_PRE_GAME: {
                    fireMessage(textMessageList, new MessageEvent(this, gameState, "assault.gamestate.pre.game"));
                    break;
                }
                case GAME_FLAG_ACTIVE: {
                    LOGGER.info("resetting game timer");
                    this.GAMETIMEINSECONDS = Integer.parseInt(MissionBox.getConfig().getProperty(MissionBox.FCY_GAMETIME)) * 60;
                    this.TIME2CAP = Integer.parseInt(MissionBox.getConfig().getProperty(MissionBox.FCY_TIME2CAPTURE));
                    fireMessage(textMessageList, new MessageEvent(this, gameState, "assault.gamestate.flag.is.active"));
                    starttime = new DateTime();
                    endtime = starttime.plusSeconds(GAMETIMEINSECONDS);
                    setGameState(GAME_FLAG_COLD);
                    break;
                }
                case GAME_FLAG_HOT: {
                    fireMessage(textMessageList, new MessageEvent(this, gameState, "assault.gamestate.flag.is.hot"));
                    flagactivation = new DateTime();
                    endtime = flagactivation.plusSeconds(TIME2CAP);
                    break;
                }
                case GAME_ROCKET_LAUNCHED: {
                    fireMessage(textMessageList, new MessageEvent(this, gameState, "assault.gamestate.rocket.launched"));
                    endtime = new DateTime();
                    rockettime = endtime.plusSeconds(rocketseconds); // hier wird noch kurz gewartet bis der raketensound abgespielt ist. danach wird gehts
//                    afterglow = rockettime + (agseconds * 1000);
                    break;
                }
                case GAME_FLAG_COLD: {
                    flagactivation = null;
                    fireMessage(textMessageList, new MessageEvent(this, gameState, "assault.gamestate.flag.is.cold"));
                    endtime = starttime.plusSeconds(GAMETIMEINSECONDS);
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

//    private synchronized void resetCycle() {
//        setCycle(BigDecimal.ZERO);
//    }
//
//    private synchronized void increaseCycle() {
//        setCycle(cycle.add(BigDecimal.ONE));
//    }
//
//    private synchronized void setCycle(BigDecimal cycle) {
//        this.cycle = cycle;
//        BigDecimal progress = cycle.divide(MAXCYCLES, 2, RoundingMode.HALF_UP).multiply(new BigDecimal(100));
//        fireMessage(percentageList, new MessageEvent(this, progress));
//    }
//
//    private boolean timeIsUp(){
//        return cycle.compareTo(MAXCYCLES) >= 0;
//    }

    @Override
    public void restartGame() {
        setGameState(GAME_PRE_GAME);
    }

    @Override
    public void startGame() {
        setGameState(GAME_FLAG_ACTIVE);
    }

    public void toggleGameSate() {
        if (gameState == GAME_PRE_GAME){
            setGameState(GAME_FLAG_ACTIVE);
        } else {
            setGameState(GAME_PRE_GAME);
        }
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
        if (gameState == GAME_PRE_GAME || gameState == GAME_OUTCOME_FLAG_TAKEN || gameState == GAME_OUTCOME_FLAG_DEFENDED || gameState == GAME_OVER)
            return;
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

            // Alle cycledivider durchgänge wird eine Nachricht bzgl. der Gametime geschickt. Sonst sind das zu viele unsinnige nachrichten.
            // cycledivider = 1000 / millispercycle
            if (threadcycles % cycledivider == 0) {

                String dateFormatted = "00:00";
                if (endtime != null && endtime.isAfterNow()) {
                    dateFormatted = endtime.minus(new DateTime().getMillis()).toString("mm:ss");
                }

                fireMessage(gameTimerList, new MessageEvent(this, gameState, dateFormatted));
            }

            try {

                if (gameState == GAME_FLAG_COLD) {
                    if (timeIsUp()) {
                        setGameState(GAME_OUTCOME_FLAG_DEFENDED);
                    }
                    fireMessage(percentageList, new MessageEvent(this, gameState, BigDecimal.ZERO));
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
                    // nach dem AfterGlow (wo musik gespielt wird), wird das Spiel auf GameOver gesetzt
//                    if (afterglow < System.currentTimeMillis()) {
                    setGameState(GAME_OVER);
//                    }
                }

                Thread.sleep(millispercycle);
            } catch (InterruptedException ie) {
                LOGGER.debug(this + " interrupted!");
            }
        }
    }
}
