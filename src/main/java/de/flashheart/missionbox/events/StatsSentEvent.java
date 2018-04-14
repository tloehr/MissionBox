package de.flashheart.missionbox.events;

import java.util.EventObject;

public class StatsSentEvent extends EventObject {
    private final boolean successful;
    private final GameEvent gameEvent;

    public StatsSentEvent(Object source, GameEvent gameEvent, boolean successful) {
        super(source);
        this.gameEvent = gameEvent;
        this.successful = successful;

    }

    public boolean isSuccessful() {
        return successful;
    }

    public GameEvent getGameEvent() {
        return gameEvent;
    }
}
