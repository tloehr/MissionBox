package interfaces;

import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import org.apache.log4j.Logger;

/**
 * Created by tloehr on 07.06.15.
 */
public class Relay implements OnOffInterface {
    private final Logger logger = Logger.getLogger(getClass());
    private final GpioPinDigitalOutput pin;

    public Relay(GpioPinDigitalOutput pin) {
        this.pin = pin;
        if (pin == null) return;
        pin.setState(PinState.LOW);
    }



    @Override
    public void setOn(boolean on) {
        if (pin == null) return;
        pin.setState(on ? PinState.HIGH : PinState.LOW);
    }

    public void blink(long l) {
//        logger.debug(String.format("blinking at %d ms", l));
        if (pin == null) return;
        pin.blink(l);
    }

    public void blink(long l, PinState pinState) {
//        logger.debug(String.format("blinking at %d ms", l));
        if (pin == null) return;
        pin.blink(l, pinState);
    }

    @Override
    public void toggle() {
//        logger.debug("relay toggle");
        if (pin == null) return;
        pin.setState(!pin.isState(PinState.HIGH));
    }
}
