package de.flashheart.missionbox.statistics;

import de.flashheart.missionbox.rlggames.GameState;

import java.util.EventObject;

public class StatsSentEvent extends EventObject {
    private final boolean successful;
    private final GameState gameState;

    public StatsSentEvent(Object source, GameState gameState, boolean successful) {
        super(source);
        this.gameState = gameState;

        this.successful = successful;

    }

    public boolean isSuccessful() {
        return successful;
    }

    public GameState getGameState() {
        return gameState;
    }
}
