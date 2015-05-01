package events;

import java.util.EventObject;

/**
 * Created by tloehr on 25.04.15.
 */
public class RespawnEvent extends EventObject {
    private int secondsElapsed;
    private int seconds2respawn;

    public RespawnEvent(Object source, int secondsElapsed, int seconds2respawn) {
        super(source);
        this.secondsElapsed = secondsElapsed;
        this.seconds2respawn = seconds2respawn;
    }
}
