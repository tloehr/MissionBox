package de.flashheart.missionbox.events;

import java.util.EventListener;

/**
 * Created by tloehr on 25.05.15.
 */
public interface MessageListener extends EventListener{
    
    void messageReceived(FC1GameEvent gameEvent);

}
