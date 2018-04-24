package de.flashheart.missionbox.events;

import de.flashheart.missionbox.Main;
import de.flashheart.missionbox.misc.Configs;
import de.flashheart.missionbox.misc.HasLogger;
import de.flashheart.missionbox.misc.Tools;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.Locale;
import java.util.Stack;

public class Statistics implements HasLogger {


    // Die hier gehören alle zur OCF Flagge. Da ich aber die beiden Clients mal vereinheitlichen will, habe ich das hier mit aufgenommen.
    public static final String EVENT_PAUSE = "EVENT_PAUSE";
    public static final String EVENT_RESUME = "EVENT_RESUME";
    public static final String EVENT_START_GAME = "EVENT_START_GAME"; // von Standby nach Active
    public static final String EVENT_BLUE_ACTIVATED = "EVENT_BLUE_ACTIVATED";
    public static final String EVENT_RED_ACTIVATED = "EVENT_RED_ACTIVATED";
    public static final String EVENT_GAME_OVER = "EVENT_GAME_OVER"; // wenn die Spielzeit abgelaufen ist
    public static final String EVENT_GAME_ABORTED = "EVENT_GAME_ABORTED"; // wenn das Spiel beendet wurde
    public static final String EVENT_RESULT_RED_WON = "EVENT_RESULT_RED_WON";
    public static final String EVENT_RESULT_BLUE_WON = "EVENT_RESULT_BLUE_WON";
    public static final String EVENT_RESULT_DRAW = "EVENT_RESULT_DRAW"; // Unentschieden
    public static final String EVENT_YELLOW_ACTIVATED = "EVENT_YELLOW_ACTIVATED";
    public static final String EVENT_GREEN_ACTIVATED = "EVENT_GREEN_ACTIVATED";
    public static final String EVENT_RESULT_GREEN_WON = "EVENT_RESULT_GREEN_WON";
    public static final String EVENT_RESULT_YELLOW_WON = "EVENT_RESULT_YELLOW_WON";
    public static final String EVENT_RESULT_MULTI_WINNERS = "EVENT_RESULT_MULTI_WINNERS"; // wenn mehr als einer die bestzeit erreicht hat (seeeeehr unwahrscheinlich)

    public static final String GAME_NON_EXISTENT = "NULL";
    public static final String GAME_PRE_GAME = "PREPGAME";
    public static final String GAME_FLAG_ACTIVE = "FLAGACTV";
    public static final String GAME_FLAG_COLD = "FLAGCOLD";
    public static final String GAME_FLAG_HOT = "FLAG_HOT";
    public static final String GAME_OUTCOME_FLAG_TAKEN = "FLAGTAKN";
    public static final String GAME_OUTCOME_FLAG_DEFENDED = "FLAGDFND";
    public static final String GAME_GOING_TO_PAUSE = "GNGPAUSE";
    public static final String GAME_PAUSING = "PAUSING"; // Box pausiert
    public static final String GAME_GOING_TO_RESUME = "GNGRESUM";
    public static final String GAME_RESUMED = "RESUMED"; // unmittelbar vor der Spielwiederaufnahme


    public Stack<GameEvent> stackEvents;
    private int matchid;
    private DateTime endOfGame = null;

    private boolean bombfused;
    private long remainingTime;
    private long captureTime, maxgametime;

    public Statistics() {
        stackEvents = new Stack<>();
        reset();
    }

    public void reset() {
        endOfGame = null;
        bombfused = false;

        remainingTime = 0l;
        captureTime = 0l;
        maxgametime = 0l;

        matchid = 0;
        stackEvents.clear();
    }

    public void updateTimers(GameEvent gameEvent) {
        this.remainingTime = gameEvent.getRemaining();
    }

    /**
     * @return true, wenn die Operation erfolgreich war.
     */
    public void sendStats() {
        getLogger().debug("sendStats()\n" + toPHP());
        if (Main.getMessageProcessor() != null)
            Main.getMessageProcessor().pushMessage(new PHPMessage(toPHP(), stackEvents.peek()));
    }

    public long addEvent(GameEvent gameEvent) {
        DateTime now = new DateTime();
        this.matchid = gameEvent.getMatchid();
        this.captureTime = ((FC1GameEvent) gameEvent).getCapturetime();
        this.maxgametime = ((FC1GameEvent) gameEvent).getMaxgametime();
        this.remainingTime = gameEvent.getRemaining();

        stackEvents.push(gameEvent);

        if (endOfGame == null) {
            if (gameEvent.getEvent() == GAME_OUTCOME_FLAG_TAKEN || gameEvent.getEvent() == GAME_OUTCOME_FLAG_DEFENDED) {
                endOfGame = now;
            }
        }

        if (gameEvent.getEvent() == GAME_FLAG_HOT) bombfused = true;
        if (gameEvent.getEvent() == GAME_FLAG_COLD) bombfused = false;


        sendStats(); // jedes Ereignis wird gesendet.

        return now.getMillis();
    }


    private String toPHP() {


        final StringBuilder php = new StringBuilder();
        php.append("<?php\n");

        String flagname = Main.getConfigs().get(Configs.FLAGNAME);

        flagname = StringUtils.replace(flagname, "'", "\\'");
        flagname = StringUtils.replace(flagname, "\"", "\\\"");

        php.append("$game['bombname'] = '" + flagname + "';\n");
        php.append("$game['uuid'] = '" + Main.getConfigs().get(Configs.MYUUID) + "';\n");
        php.append("$game['matchid'] = '" + matchid + "';\n");
        php.append("$game['timestamp'] = '" + DateTimeFormat.mediumDateTime().withLocale(Locale.getDefault()).print(new DateTime()) + "';\n");
        php.append("$game['ts_game_started'] = '" + DateTimeFormat.mediumDateTime().withLocale(Locale.getDefault()).print(stackEvents.get(0).getPit()) + "';\n");
        php.append("$game['ts_game_paused'] = '" + (stackEvents.peek().getEvent().equals(EVENT_PAUSE) ? DateTimeFormat.mediumDateTime().withLocale(Locale.getDefault()).print(stackEvents.peek().getPit()) : "null") + "';\n");
        php.append("$game['ts_game_ended'] = '" + (endOfGame == null ? "null" : DateTimeFormat.mediumDateTime().withLocale(Locale.getDefault()).print(endOfGame)) + "';\n");
        php.append("$game['bombfused'] = '" + Boolean.toString(bombfused) + "';\n");
        php.append("$game['remaining'] = '" + Tools.formatLongTime(remainingTime, "HH:mm:ss") + "';\n");
        php.append("$game['capturetime'] = '" + Tools.formatLongTime(captureTime, "HH:mm:ss") + "';\n");
        php.append("$game['maxgametime'] = '" + Tools.formatLongTime(maxgametime, "HH:mm:ss") + "';\n");
        // ist eigentlich überflüssig. macht aber den PHP code leichter.
        php.append("$game['winner'] = '" + (endOfGame == null ? "notdecidedyet" : (bombfused ? "attacker" : "defender")) + "';\n");

        php.append("$game['events'] = [\n");
        for (GameEvent event : stackEvents) {
            php.append(event.toPHPArray());
        }

        php.append("];\n?>");


        return php.toString();
    }


}
