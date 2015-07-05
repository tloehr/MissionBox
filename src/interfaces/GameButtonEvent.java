package interfaces;

import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import org.apache.log4j.Logger;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by tloehr on 05.07.15.
 */
public class GameButtonEvent {

    protected final Logger logger = Logger.getLogger(this.getClass());

    public static final int BUTTON_UP = 0;
    public static final int BUTTON_DOWN = 1;

    protected int buttonState = BUTTON_UP;

    public GameButtonEvent(GpioPinDigitalStateChangeEvent event) {
        buttonState = event.getState() == PinState.HIGH ? BUTTON_DOWN : BUTTON_UP;
        logger.debug("button " + event.getSource() + ", state changed to " + (buttonState == BUTTON_UP ? "UP" : "DOWN"));
    }

    public GameButtonEvent(MouseEvent event) {
        if (event.getID() == MouseEvent.MOUSE_PRESSED) buttonState = BUTTON_DOWN;
        if (event.getID() == MouseEvent.MOUSE_RELEASED) buttonState = BUTTON_UP;
        logger.debug("button " + event.getSource() + ", state changed to " + (buttonState == BUTTON_UP ? "UP" : "DOWN"));
    }
}
