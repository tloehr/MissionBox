package interfaces;

import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import main.MissionBox;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;

/**
 * Created by tloehr on 07.06.15.
 */
public class Relay implements OnOffInterface {
    private final Logger logger = Logger.getLogger(getClass());
    private final GpioPinDigitalOutput pin;
    private final String name;
    private MyLED debugLED; // for on screen debugging

    public Relay(GpioPinDigitalOutput pin, String name) {
        this.pin = pin;
        this.name = name;
        if (pin != null) pin.setState(PinState.LOW);
    }

    /**
     *
     * @param pin
     * @param name
     * @param color the color of the on screen visualisation for this relay
     * @param addYourself2this
     */
    public Relay(GpioPinDigitalOutput pin, String name, Color color, JPanel addYourself2this) {
        this(pin, name);
        debugLED = new MyLED(name, color);
        addYourself2this.add(debugLED);
    }

    public Relay(String configKey, Color color, JPanel addYourself2this) {
            this(MissionBox.getOutputMap().get(configKey), configKey);
            debugLED = new MyLED(name, color);
            addYourself2this.add(debugLED);
        }

    public void setText(String text) {
        if (text.isEmpty()) {
            debugLED.setText(name);
        } else {
            debugLED.setText(name + " [" + text + "]");
        }

    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setOn(boolean on) {
        if (pin != null) pin.setState(on ? PinState.HIGH : PinState.LOW);
        if (debugLED != null) debugLED.setOn(on);
        logger.debug(name + " " + (on ? "on" : "off"));
    }

}
