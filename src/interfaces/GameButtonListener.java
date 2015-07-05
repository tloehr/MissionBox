package interfaces;

import java.util.EventListener;

/**
 * Created by tloehr on 05.07.15.
 */
public interface GameButtonListener extends EventListener {

    void buttonDown(GameButtonEvent gameButtonEvent);

    void buttonUp(GameButtonEvent gameButtonEvent);

}
