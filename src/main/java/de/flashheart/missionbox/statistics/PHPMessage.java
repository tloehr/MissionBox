package de.flashheart.missionbox.statistics;

import de.flashheart.missionbox.rlggames.GameEvent;

public class PHPMessage {
    String php;

    GameEvent gameEvent;

    public PHPMessage(String php, GameEvent gameEvent) {
        this.php = php;
        this.gameEvent = gameEvent;

    }

    public String getPhp() {
        return php;
    }


    public GameEvent getGameEvent() {
        return gameEvent;
    }

    @Override
    public String toString() {
        return "PHPMessage{" +
                "php='" + php + '\'' +
                ", gameEvent=" + gameEvent +
                '}';
    }
}
