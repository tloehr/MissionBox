package de.flashheart.missionbox.statistics;

import de.flashheart.missionbox.Main;
import de.flashheart.missionbox.events.FC1GameEvent;
import de.flashheart.missionbox.misc.Configs;
import de.flashheart.missionbox.misc.HasLogger;
import de.flashheart.missionbox.rlggames.GameEvent;
import de.flashheart.missionbox.rlggames.GameState;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;

public class Statistics implements HasLogger {

    private final MessageProcessor messageProcessor;
    private final HashMap<String, String> stateColors, stateDisplayText;
    private GameState gameState;

    public Statistics() {
        messageProcessor = Main.getMessageProcessor();
        stateColors = GameState.getStateColors();
        stateDisplayText = GameState.getEvent2State();

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

        // these event are propagated to the STATE attribute
        if (stateColors.containsKey(fc1GameEvent.getEvent())) {
            gameState.setState(stateDisplayText.get(fc1GameEvent.getEvent()));
            gameState.setColor(stateColors.get(fc1GameEvent.getEvent()));
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


}
