package interfaces;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Created by tloehr on 07.06.15.
 */
public class RelaySiren implements PercentageInterface {

    private final GpioController GPIO;
    private final ArrayList<GpioPinDigitalOutput> myRelais;
    private Logger logger = Logger.getLogger(this.getClass());

    public RelaySiren(GpioController GPIO, ArrayList<GpioPinDigitalOutput> myRelais) {
        this.GPIO = GPIO;
        this.myRelais = myRelais;
    }

    @Override
    public void setValue(BigDecimal percent) {
        int relaynum = new BigDecimal(myRelais.size()).divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP).multiply(percent).intValue();

        logger.debug(relaynum);

        if (relaynum >= 100) {
            for (int relay = 0; relay < myRelais.size(); relay++) {
                myRelais.get(relay).setState(false);
            }
            myRelais.get(myRelais.size() - 1).setState(PinState.HIGH);

        } else {
            for (int relay = 0; relay < myRelais.size(); relay++) {
                myRelais.get(relay).setState(percent.compareTo(BigDecimal.ZERO) > 0 && relaynum == relay);
            }
        }


    }
}
