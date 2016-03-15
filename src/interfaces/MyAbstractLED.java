package interfaces;

import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import org.apache.log4j.Logger;

/**
 * Created by tloehr on 15.03.16.
 */
public class MyAbstractLED {
    private final Logger logger = Logger.getLogger(getClass());
    GpioPinDigitalOutput led;

    public MyAbstractLED(GpioPinDigitalOutput led) {
        this.led = led;
    }

    public void blink(long l) {
        logger.debug(String.format("blinking at %d ms", l));
        if (led == null) return;
        led.blink(l);
    }

    public void blink(long l, PinState pinState) {
        logger.debug(String.format("blinking at %d ms", l));
        if (led == null) return;
        led.blink(l, pinState);
    }
}
