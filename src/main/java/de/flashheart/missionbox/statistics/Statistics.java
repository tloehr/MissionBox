package de.flashheart.missionbox.statistics;

import de.flashheart.missionbox.Main;
import de.flashheart.missionbox.events.FC1GameEvent;
import de.flashheart.missionbox.misc.Configs;
import de.flashheart.missionbox.misc.HasLogger;
import org.joda.time.DateTime;

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
    public static final String GAME_SUDDEN_DEATH = "SDDNDEATH";
    public static final String GAME_OVERTIME = "OVRTIME";
    public static final String GAME_OUTCOME_FLAG_TAKEN = "FLAGTAKN";
    public static final String GAME_OUTCOME_FLAG_DEFENDED = "FLAGDFND";
    public static final String GAME_GOING_TO_PAUSE = "GNGPAUSE";
    public static final String GAME_PAUSING = "PAUSING"; // Box pausiert
    public static final String GAME_GOING_TO_RESUME = "GNGRESUM";
    public static final String GAME_RESUMED = "RESUMED"; // unmittelbar vor der Spielwiederaufnahme
    private final MessageProcessor messageProcessor;


    private GameState gameState;
    private DateTime endOfGame = null;

    private long min_stat_send_time;
    private boolean bombfused;
    private long remainingTime;
    private long captureTime, maxgametime, gametime;

    public Statistics() {
        messageProcessor = Main.getMessageProcessor();
        reset();
    }

    public void reset() {
        min_stat_send_time = Long.parseLong(Main.getConfigs().get(Configs.MIN_STAT_SEND_TIME));
        endOfGame = null;
        bombfused = false;

        gametime = 0l;
        remainingTime = 0l;
        captureTime = 0l;
        maxgametime = 0l;

        gameState = new GameState(Main.getConfigs().get(Configs.FLAGNAME), GameState.TYPE_FARCRY, Main.getConfigs().get(Configs.MYUUID), Main.getConfigs().getNextMatchID());

    }

    public void updateTimers(FC1GameEvent gameEvent) {
        this.remainingTime = gameEvent.getRemaining();
        this.gametime = gameEvent.getGametime();
    }

    /**
     * @return true, wenn die Operation erfolgreich war.
     */
    public void sendStats() {
        getLogger().debug("sendStats()\n" + gameState);
        if (min_stat_send_time > 0)
            messageProcessor.pushMessage(gameState);
    }

    public long addEvent(FC1GameEvent fc1GameEvent) {
        DateTime now = new DateTime();
        this.captureTime = fc1GameEvent.getCapturetime();
        this.maxgametime = fc1GameEvent.getMaxgametime();
        this.remainingTime = fc1GameEvent.getRemaining();
        this.gametime = fc1GameEvent.getGametime();

        gameState.getGameEvents().add(fc1GameEvent.createGameEvent());

//        stackEvents.push(gameEvent);

        if (endOfGame == null) {
            if (fc1GameEvent.getEvent() == GAME_OUTCOME_FLAG_TAKEN || fc1GameEvent.getEvent() == GAME_OUTCOME_FLAG_DEFENDED) {
                endOfGame = now;
            }
        }

        if (fc1GameEvent.getEvent() == GAME_FLAG_HOT) bombfused = true;
        if (fc1GameEvent.getEvent() == GAME_FLAG_COLD) bombfused = false;


        sendStats(); // jedes Ereignis wird gesendet.

        return now.getMillis();
    }


//    private String toPHP() {
//        final StringBuilder php = new StringBuilder();
//        php.append("<?php\n");
//
//        String flagname = Main.getConfigs().get(Configs.FLAGNAME);
//
//        flagname = StringUtils.replace(flagname, "'", "\\'");
//        flagname = StringUtils.replace(flagname, "\"", "\\\"");
//
//        php.append("$game['bombname'] = '" + flagname + "';\n");
//        php.append("$game['uuid'] = '" + Main.getConfigs().get(Configs.MYUUID) + "';\n");
//        php.append("$game['matchid'] = '" + matchid + "';\n");
//        php.append("$game['timestamp'] = '" + DateTimeFormat.mediumDateTime().withLocale(Locale.getDefault()).print(new DateTime()) + "';\n");
//        php.append("$game['ts_game_started'] = '" + DateTimeFormat.mediumDateTime().withLocale(Locale.getDefault()).print(stackEvents.get(0).getPit()) + "';\n");
//        php.append("$game['ts_game_paused'] = '" + (stackEvents.peek().getEvent().equals(EVENT_PAUSE) ? DateTimeFormat.mediumDateTime().withLocale(Locale.getDefault()).print(stackEvents.peek().getPit()) : "null") + "';\n");
//        php.append("$game['ts_game_ended'] = '" + (endOfGame == null ? "null" : DateTimeFormat.mediumDateTime().withLocale(Locale.getDefault()).print(endOfGame)) + "';\n");
//        php.append("$game['bombfused'] = '" + Boolean.toString(bombfused) + "';\n");
//        php.append("$game['remaining'] = '" + Tools.formatLongTime(remainingTime, "HH:mm:ss") + "';\n");
//        php.append("$game['capturetime'] = '" + Tools.formatLongTime(captureTime, "HH:mm:ss") + "';\n");
//        php.append("$game['maxgametime'] = '" + Tools.formatLongTime(maxgametime, "HH:mm:ss") + "';\n");
//        php.append("$game['gametime'] = '" + Tools.formatLongTime(gametime, "HH:mm:ss") + "';\n");
//        // ist eigentlich überflüssig. macht aber den PHP code leichter.
//        php.append("$game['winner'] = '" + (endOfGame == null ? "notdecidedyet" : (bombfused ? "attacker" : "defender")) + "';\n");
//        php.append("$game['overtime'] = '" + (gametime > maxgametime ? Tools.formatLongTime(gametime - maxgametime, "HH:mm:ss") : "--") + "';\n");
//
//        php.append("$game['events'] = [\n");
//        for (GameEvent event : stackEvents) {
//            php.append(event.toPHPArray());
//        }
//
//        php.append("];\n?>");
//
//
//        return php.toString();
//    }


}
