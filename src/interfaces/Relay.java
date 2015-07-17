package interfaces;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;

/**
 * Created by tloehr on 07.06.15.
 */
public class Relay implements OnOffInterface {



    private final GpioPinDigitalOutput pin;

    public Relay(GpioPinDigitalOutput pin) {

        this.pin = pin;
        pin.setState(PinState.LOW);
    }

    @Override
    public void setOn(boolean on) {
        pin.setState(on);
    }


    public void setOn() {
        pin.setState(true);
    }

    public void setOff() {
        pin.setState(false);
    }


    @Override
    public void toggle() {
        pin.setState(!pin.isState(PinState.HIGH));
    }
}
