package de.flashheart.missionbox.rlggames;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GameState {

    // for gametype
    public static final String TYPE_FARCRY = "farcry";
    public static final String TYPE_CENTERFLAG = "centerflag";

    public static final String EVENT_PREPARATION = "event_pregame";
    public static final String EVENT_PAUSING = "event_pausing";
    public static final String EVENT_STILL_RUNNING = "event_still_running";
    public static final String EVENT_ATTACKERS_WON = "event_attackers_won";
    public static final String EVENT_DEFENDERS_WON = "event_defenders_won";
    public static final String EVENT_MULTI_WINNERS = "event_multiple_winners";
    public static final String EVENT_RED_TEAM_WON = "event_red_won";
    public static final String EVENT_BLUE_TEAM_WON = "event_blue_won";
    public static final String EVENT_YELLOW_TEAM_WON = "event_yellow_won";
    public static final String EVENT_GREEN_TEAM_WON = "event_green_won";
    public static final String EVENT_GAME_ABORTED = "event_abort";
    public static final String EVENT_FUSED = "event_fused";
    public static final String EVENT_DEFUSED = "event_defused";
    public static final String EVENT_RED_ACTIVE = "event_red_active";
    public static final String EVENT_BLUE_ACTIVE = "event_blue_active";
    public static final String EVENT_YELLOW_ACTIVE = "event_yellow_active";
    public static final String EVENT_GREEN_ACTIVE = "event_green_active";

    public static final String[] GAME_OVER_STATES = new String[]{EVENT_ATTACKERS_WON, EVENT_BLUE_TEAM_WON, EVENT_DEFENDERS_WON, EVENT_GAME_ABORTED, EVENT_GREEN_TEAM_WON, EVENT_MULTI_WINNERS, EVENT_RED_TEAM_WON, EVENT_GREEN_TEAM_WON, EVENT_YELLOW_TEAM_WON};

    private String bombname;
    private String gametype;
    private String state;
    private String uuid;
    private long matchid;
    private long timestamp;
    private long timestamp_game_started;
    private long timestamp_game_paused;
    private long timestamp_game_ended;
    private boolean bombfused;
    private long remaining;
    private long capturetime;
    private long maxgametime;
    private long gametime;
    private List<GameEvent> gameEvents;
    private String zoneid; // for timezone handling
    private String color;

    public GameState() {
        gameEvents = new ArrayList<>();
    }

    public GameState(String bombname, String gametype, String uuid, long matchid) {
        this();
        this.state = EVENT_PREPARATION;
        this.bombfused = false;
        this.bombname = bombname;
        this.gametype = gametype;
        this.uuid = uuid;
        this.matchid = matchid;
        this.capturetime = 0l;
        this.maxgametime = 0l;
        this.remaining = 0l;
        this.bombfused = false;
        this.timestamp_game_started = -1;
        this.timestamp_game_paused = -1;
        this.timestamp_game_ended = -1;
        this.timestamp = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        this.zoneid = ZoneId.systemDefault().getId();
        this.color = "white";
    }

    public String getBombname() {
        return bombname;
    }

    public void setBombname(String bombname) {
        this.bombname = bombname;
    }

    public String getGametype() {
        return gametype;
    }

    public void setGametype(String gametype) {
        this.gametype = gametype;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public long getMatchid() {
        return matchid;
    }

    public void setMatchid(long matchid) {
        this.matchid = matchid;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestamp_game_started() {
        return timestamp_game_started;
    }

    public void setTimestamp_game_started(long timestamp_game_started) {
        this.timestamp_game_started = timestamp_game_started;
    }

    public long getTimestamp_game_paused() {
        return timestamp_game_paused;
    }

    public void setTimestamp_game_paused(long timestamp_game_paused) {
        this.timestamp_game_paused = timestamp_game_paused;
    }

    public long getTimestamp_game_ended() {
        return timestamp_game_ended;
    }

    public void setTimestamp_game_ended(long timestamp_game_ended) {
        this.timestamp_game_ended = timestamp_game_ended;
    }

    public boolean isBombfused() {
        return bombfused;
    }

    public void setBombfused(boolean bombfused) {
        this.bombfused = bombfused;
    }

    public long getRemaining() {
        return remaining;
    }

    public void setRemaining(long remaining) {
        this.remaining = remaining;
    }

    public long getCapturetime() {
        return capturetime;
    }

    public void setCapturetime(long capturetime) {
        this.capturetime = capturetime;
    }

    public long getMaxgametime() {
        return maxgametime;
    }

    public void setMaxgametime(long maxgametime) {
        this.maxgametime = maxgametime;
    }

    public long getGametime() {
        return gametime;
    }

    public void setGametime(long gametime) {
        this.gametime = gametime;
    }

    public List<GameEvent> getGameEvents() {
        return gameEvents;
    }

    public void setGameEvents(List<GameEvent> gameEvents) {
        this.gameEvents = gameEvents;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZoneid() {
        return zoneid;
    }

    public void setZoneid(String zoneid) {
        this.zoneid = zoneid;
    }

    @Override
    public String toString() {
        return "GameState{" +
                "bombname='" + bombname + '\'' +
                ", gametype='" + gametype + '\'' +
                ", state='" + state + '\'' +
                ", uuid='" + uuid + '\'' +
                ", matchid=" + matchid +
                ", timestamp=" + timestamp +
                ", timestamp_game_started=" + timestamp_game_started +
                ", timestamp_game_paused=" + timestamp_game_paused +
                ", timestamp_game_ended=" + timestamp_game_ended +
                ", bombfused=" + bombfused +
                ", remaining=" + remaining +
                ", capturetime=" + capturetime +
                ", maxgametime=" + maxgametime +
                ", gametime=" + gametime +
                ", gameEvents=" + gameEvents +
                ", zoneid='" + zoneid + '\'' +
                ", color='" + color + '\'' +
                '}';
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public static HashMap<String, String> getStateColors() {
        HashMap stateColors = new HashMap();
        stateColors.put(GameEvent.FUSED, "red");
        stateColors.put(GameEvent.DEFUSED, "green");
        stateColors.put(GameEvent.PREGAME, "blue");
        stateColors.put(GameEvent.PAUSING, "blue");
        stateColors.put(GameEvent.EXPLODED, "red");
        stateColors.put(GameEvent.DEFENDED, "green");
        stateColors.put(GameEvent.START_GAME, "white");
        stateColors.put(GameEvent.GAME_ABORTED, "white");
        return stateColors;
    }

    public static HashMap<String, String> getEvent2State() {
        HashMap stateDisplayText = new HashMap();
        stateDisplayText.put(GameEvent.FUSED, EVENT_FUSED);
        stateDisplayText.put(GameEvent.DEFUSED, EVENT_DEFUSED);
        stateDisplayText.put(GameEvent.PREGAME, EVENT_PREPARATION);
        stateDisplayText.put(GameEvent.PAUSING, EVENT_PAUSING);
        stateDisplayText.put(GameEvent.EXPLODED, EVENT_ATTACKERS_WON);
        stateDisplayText.put(GameEvent.DEFENDED, EVENT_DEFENDERS_WON);
        stateDisplayText.put(GameEvent.START_GAME, EVENT_STILL_RUNNING);
        stateDisplayText.put(GameEvent.GAME_ABORTED, EVENT_GAME_ABORTED);
        return stateDisplayText;
    }
}
