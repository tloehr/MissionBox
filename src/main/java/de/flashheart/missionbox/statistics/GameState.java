package de.flashheart.missionbox.statistics;

import java.util.ArrayList;
import java.util.List;

public class GameState {

    // for gamestate
    public static final String NON_EXISTENT = "NULL";
    public static final String PRE_GAME = "PREPGAME";
    public static final String FLAG_ACTIVE = "FLAGACTV";
    public static final String FLAG_COLD = "FLAGCOLD";
    public static final String FLAG_HOT = "FLAG_HOT";
    public static final String SUDDEN_DEATH = "SDDNDEATH";
    public static final String OVERTIME = "OVRTIME";
    public static final String OUTCOME_FLAG_TAKEN = "FLAGTAKN";
    public static final String OUTCOME_FLAG_DEFENDED = "FLAGDFND";
    public static final String GOING_TO_PAUSE = "GNGPAUSE";
    public static final String PAUSING = "PAUSING"; // Box pausiert
    public static final String GOING_TO_RESUME = "GNGRESUM";
    public static final String RESUMED = "RESUMED"; // unmittelbar vor der Spielwiederaufnahme

    // for gametype
    public static final String TYPE_FARCRY = "farcry";
    public static final String TYPE_CENTERFLAG = "centerflag";

    private String bombname;
    private String gametype;
    private String gamestate;
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

    public GameState() {
        gameEvents = new ArrayList<>();
        gametime = 0l;
        remaining = 0l;
        capturetime = 0l; // farcry only
        maxgametime = 0l;
        timestamp = 0l;
        timestamp_game_started = -1l;
        timestamp_game_paused = -1l;
        timestamp_game_ended = -1l;
    }

    public GameState(String bombname, String gametype, String uuid, long matchid) {
        this();
        this.bombname = bombname;
        this.gametype = gametype;
        this.uuid = uuid;
        this.matchid = matchid;
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

    public String getGamestate() {
        return gamestate;
    }

    public void setGamestate(String gamestate) {
        this.gamestate = gamestate;
    }

    @Override
    public String toString() {
        return "GameState{" +
                "bombname='" + bombname + '\'' +
                ", gametype='" + gametype + '\'' +
                ", gamestate='" + gamestate + '\'' +
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
                '}';
    }
}
