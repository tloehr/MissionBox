package progresshandlers;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import interfaces.PercentageInterface;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Created by tloehr on 07.06.15.
 */
public class RelaySirenBlinking extends PercentageInterface {


    private final ArrayList<GpioPinDigitalOutput> myRelais;
    private Logger logger = Logger.getLogger(this.getClass());

    public RelaySirenBlinking(ArrayList<GpioPinDigitalOutput> myRelais) {
        super("Blinking Sirens");
        this.myRelais = myRelais;
    }

    @Override
    public void setValue(BigDecimal percent) {
        int relaynum = new BigDecimal(myRelais.size()).divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP).multiply(percent).intValue();

        if (relaynum >= 100) {
            for (int relay = 0; relay < myRelais.size(); relay++) {
                myRelais.get(relay).setState(false);
            }
        } else {
            for (int relay = 0; relay < myRelais.size(); relay++) {
                myRelais.get(relay).setState(percent.compareTo(BigDecimal.ZERO) > 0 && relaynum == relay);
            }
        }
    }
}
