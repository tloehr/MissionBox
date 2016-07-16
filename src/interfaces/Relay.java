package interfaces;

import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import org.apache.log4j.Logger;

import javax.swing.*;

/**
 * Created by tloehr on 07.06.15.
 */
public class Relay implements OnOffInterface {
    private final Logger logger = Logger.getLogger(getClass());
    private final GpioPinDigitalOutput pin;
    private final String name;
    private JCheckBox cbRelay; // for on screen debugging

    public Relay(GpioPinDigitalOutput pin, String name) {
        this.pin = pin;
        this.name = name;
        if (pin != null) pin.setState(PinState.LOW);
    }

    public Relay(GpioPinDigitalOutput pin, String name,  JPanel addYourself2this) {
        this(pin, name);
        cbRelay = new JCheckBox(name);
        addYourself2this.add(cbRelay);
    }

    public void setText(String text){
        if (text.isEmpty()){
            cbRelay.setText(name);
        } else {
            cbRelay.setText(name +" ["+text+"]");
        }

    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setOn(boolean on) {
        if (pin != null) pin.setState(on ? PinState.HIGH : PinState.LOW);
        if (cbRelay != null) cbRelay.setSelected(on);
        logger.debug(name + " " + (on ? "on" : "off"));
    }

}
