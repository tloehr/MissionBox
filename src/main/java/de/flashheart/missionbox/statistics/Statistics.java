package de.flashheart.missionbox.statistics;

import de.flashheart.gamestate.GameEvent;
import de.flashheart.gamestate.GameState;
import de.flashheart.missionbox.Main;
import de.flashheart.missionbox.events.FC1GameEvent;
import de.flashheart.missionbox.misc.Configs;
import de.flashheart.missionbox.misc.HasLogger;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;

public class Statistics implements HasLogger {
    public static final String[] EVENTS_TO_STATE = new String[]{GameEvent.GAME_ABORTED, GameEvent.DEFENDED, GameEvent.EXPLODED};

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
        gameState.setGametime(gameEvent.getGametime());
        gameState.setRemaining(gameEvent.getRemaining());
        gameState.setCapturetime(gameEvent.getCapturetime());
        gameState.setTimestamp_game_started(gameEvent.getStarttime());
        gameState.setTimestamp_game_paused(gameEvent.getPausingSince());
        gameState.setTimestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
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
            if (fc1GameEvent.getEvent().equals(GameEvent.EXPLODED)
                    || fc1GameEvent.getEvent().equals(GameEvent.DEFENDED)
                    || fc1GameEvent.getEvent().equals(GameEvent.GAME_ABORTED)) {
                gameState.setTimestamp_game_ended(now);
            }
        }

        gameState.setColor("white");

        // Result ?
        if (Arrays.asList(EVENTS_TO_STATE).contains(fc1GameEvent.getEvent())) {
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

        if (fc1GameEvent.getEvent() == GameEvent.FUSED) {
            gameState.setBombfused(true);
            gameState.setColor("red");
        }
        if (fc1GameEvent.getEvent() == GameEvent.DEFUSED) {
            gameState.setBombfused(false);
            gameState.setColor("green");
        }

        sendStats(); // jedes neue Ereignis wird gesendet.

        return now;
    }


}
