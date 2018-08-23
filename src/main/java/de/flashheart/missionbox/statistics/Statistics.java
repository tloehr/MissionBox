package de.flashheart.missionbox.statistics;

import de.flashheart.missionbox.Main;
import de.flashheart.missionbox.events.FC1GameEvent;
import de.flashheart.missionbox.misc.Configs;
import de.flashheart.missionbox.misc.HasLogger;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class Statistics implements HasLogger {


    //    // Die hier gehören alle zur OCF Flagge. Da ich aber die beiden Clients mal vereinheitlichen will, habe ich das hier mit aufgenommen.
//    public static final String EVENT_PAUSE = "EVENT_PAUSE";
//    public static final String EVENT_RESUME = "EVENT_RESUME";
//    public static final String EVENT_START_GAME = "EVENT_START_GAME"; // von Standby nach Active
//    public static final String EVENT_BLUE_ACTIVATED = "EVENT_BLUE_ACTIVATED";
//    public static final String EVENT_RED_ACTIVATED = "EVENT_RED_ACTIVATED";
//    public static final String EVENT_GAME_OVER = "EVENT_GAME_OVER"; // wenn die Spielzeit abgelaufen ist
//    public static final String EVENT_GAME_ABORTED = "EVENT_GAME_ABORTED"; // wenn das Spiel beendet wurde
//    public static final String EVENT_RESULT_RED_WON = "EVENT_RESULT_RED_WON";
//    public static final String EVENT_RESULT_BLUE_WON = "EVENT_RESULT_BLUE_WON";
//    public static final String EVENT_RESULT_DRAW = "EVENT_RESULT_DRAW"; // Unentschieden
//    public static final String EVENT_YELLOW_ACTIVATED = "EVENT_YELLOW_ACTIVATED";
//    public static final String EVENT_GREEN_ACTIVATED = "EVENT_GREEN_ACTIVATED";
//    public static final String EVENT_RESULT_GREEN_WON = "EVENT_RESULT_GREEN_WON";
//    public static final String EVENT_RESULT_YELLOW_WON = "EVENT_RESULT_YELLOW_WON";
//    public static final String EVENT_RESULT_MULTI_WINNERS = "EVENT_RESULT_MULTI_WINNERS"; // wenn mehr als einer die bestzeit erreicht hat (seeeeehr unwahrscheinlich)
//
//    public static final String GAME_NON_EXISTENT = "NULL";
//    public static final String GAME_PRE_GAME = "PREPGAME";
//    public static final String GAME_FLAG_ACTIVE = "FLAGACTV";
//    public static final String GAME_FLAG_COLD = "FLAGCOLD";
//    public static final String GAME_FLAG_HOT = "FLAG_HOT";
//    public static final String GAME_SUDDEN_DEATH = "SDDNDEATH";
//    public static final String GAME_OVERTIME = "OVRTIME";
//    public static final String GAME_OUTCOME_FLAG_TAKEN = "FLAGTAKN";
//    public static final String GAME_OUTCOME_FLAG_DEFENDED = "FLAGDFND";
//    public static final String GAME_GOING_TO_PAUSE = "GNGPAUSE";
//    public static final String GAME_PAUSING = "PAUSING"; // Box pausiert
//    public static final String GAME_GOING_TO_RESUME = "GNGRESUM";
//    public static final String GAME_RESUMED = "RESUMED"; // unmittelbar vor der Spielwiederaufnahme
    private final MessageProcessor messageProcessor;


    private GameState gameState;

    public Statistics() {
        messageProcessor = Main.getMessageProcessor();
        reset();
    }

    public void reset() {
        gameState = new GameState(Main.getConfigs().get(Configs.FLAGNAME), GameState.TYPE_FARCRY, Main.getConfigs().get(Configs.MYUUID), Main.getConfigs().getNextMatchID());
    }

    public void updateTimers(FC1GameEvent gameEvent) {
        gameState.setRemaining(gameEvent.getRemaining());
        gameState.setGametime(gameEvent.getGametime());
        gameState.setRemaining(gameEvent.getRemaining());
        gameState.setCapturetime(gameEvent.getCapturetime());
        gameState.setTimestamp_game_started(gameEvent.getStarttime());
        gameState.setTimestamp_game_paused(gameEvent.getPausingSince());
        gameState.setTimestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());

        getLogger().debug(gameState.getTimestamp());
        getLogger().debug(System.currentTimeMillis());

    }

    /**
     * @return true, wenn die Operation erfolgreich war.
     */
    public void sendStats() {
        messageProcessor.pushMessage(gameState);
    }

    public long addEvent(FC1GameEvent fc1GameEvent) {
        long now = System.currentTimeMillis();
        updateTimers(fc1GameEvent);

        if (gameState.getGameEvents().isEmpty()) {
            gameState.setTimestamp_game_started(now);
        }

        gameState.getGameEvents().add(fc1GameEvent.createGameEvent());


        if (gameState.getTimestamp_game_ended() == -1l) {
            if (fc1GameEvent.getEvent().equals(GameEvent.EXPLODED) || fc1GameEvent.getEvent().equals(GameEvent.DEFENDED)) {
                gameState.setTimestamp_game_ended(now);
            }
        }

        if (fc1GameEvent.getEvent() == GameEvent.PAUSING ||
                fc1GameEvent.getEvent() == GameEvent.FUSED ||
                fc1GameEvent.getEvent() == GameEvent.DEFUSED ||
                fc1GameEvent.getEvent() == GameEvent.PREGAME ||
                fc1GameEvent.getEvent() == GameEvent.EXPLODED ||
                fc1GameEvent.getEvent() == GameEvent.DEFENDED
        ) {
            gameState.setState(fc1GameEvent.getEvent());
        }

        if (fc1GameEvent.getEvent() == GameEvent.PAUSING) {
            gameState.setTimestamp_game_paused(now);
        } else {
            gameState.setTimestamp_game_paused(-1l);
        }

        if (fc1GameEvent.getEvent() == GameEvent.EXPLODED ||
                fc1GameEvent.getEvent() == GameEvent.DEFENDED) {
            gameState.setTimestamp_game_ended(now);
        }

        if (fc1GameEvent.getEvent() == GameEvent.FUSED) gameState.setBombfused(true);
        if (fc1GameEvent.getEvent() == GameEvent.DEFUSED) gameState.setBombfused(false);

        sendStats(); // jedes neue Ereignis wird gesendet.

        return now;
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
