package de.flashheart.missionbox.statistics;

/**
 * Created by tloehr on 25.04.15.
 */
public class GameEvent {

    public static final String PREGAME = "PREGAME";
    public static final String RESUMED = "RESUMED";
    public static final String FUSED = "FUSED";
    public static final String DEFUSED = "DEFUSED";
    public static final String START_GAME = "START_GAME"; // von Standby nach Active
    public static final String BLUE_ACTIVATED = "BLUE_ACTIVATED";
    public static final String RED_ACTIVATED = "RED_ACTIVATED";
    public static final String GAME_OVER = "GAME_OVER"; // wenn die Spielzeit abgelaufen ist
    public static final String GAME_ABORTED = "GAME_ABORTED"; // wenn das Spiel beendet wurde
    public static final String RESULT_RED_WON = "RESULT_RED_WON";
    public static final String RESULT_BLUE_WON = "RESULT_BLUE_WON";
    public static final String RESULT_DRAW = "RESULT_DRAW"; // Unentschieden
    public static final String YELLOW_ACTIVATED = "YELLOW_ACTIVATED";
    public static final String GREEN_ACTIVATED = "GREEN_ACTIVATED";
    public static final String RESULT_GREEN_WON = "RESULT_GREEN_WON";
    public static final String RESULT_YELLOW_WON = "RESULT_YELLOW_WON";
    public static final String RESULT_MULTI_WINNERS = "RESULT_MULTI_WINNERS"; // wenn mehr als einer die bestzeit erreicht hat (seeeeehr unwahrscheinlich)
    public static final String EXPLODED = "EXPLODED";
    public static final String DEFENDED = "DEFENDED";

    public static final String GOING_TO_PAUSE = "GNGPAUSE";
    public static final String PAUSING = "PAUSING"; // Box pausiert
    public static final String GOING_TO_RESUME = "GNGRESUM";


    protected long pit;
    protected String event;
    protected long gametime;
    protected long remaining;

    public GameEvent() {
    }

    public GameEvent(long pit, String event, long gametime, long remaining) {
        this.pit = pit;
        this.event = event;
        this.gametime = gametime;
        this.remaining = remaining;
    }

    public long getPit() {
        return pit;
    }

    public void setPit(long pit) {
        this.pit = pit;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public long getGametime() {
        return gametime;
    }

    public void setGametime(long gametime) {
        this.gametime = gametime;
    }

    public long getRemaining() {
        return remaining;
    }

    public void setRemaining(long remaining) {
        this.remaining = remaining;
    }

    @Override
    public String toString() {
        return "GameEvent{" +
                "pit=" + pit +
                ", event='" + event + '\'' +
                ", gametime=" + gametime +
                ", remaining=" + remaining +
                '}';
    }

}
