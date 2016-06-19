package interfaces;

import com.pi4j.io.gpio.PinState;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Created by Torsten on 19.06.2016.
 */
public class RelaySirenPulse extends RelaySiren {
    long pulsetimeinmillis = 1000;


    public RelaySirenPulse(ArrayList<Relay> myRelais, long pulsetimeinmillis) {
        super(myRelais);
        this.pulsetimeinmillis = pulsetimeinmillis;
    }

    @Override
    public void setValue(BigDecimal percent) {

        lastChangeTime = System.currentTimeMillis();



        int relaynum = new BigDecimal(myRelais.size()).divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP).multiply(percent).intValue();

        logger.debug(percent);
        logger.debug("relaynum" + relaynum + " out of " + myRelais.size());

        if (relaynum >= myRelais.size()) {
            for (int relay = 0; relay < myRelais.size(); relay++) {
                myRelais.get(relay).setOn(false);
            }
            // leave the last one on, when 100 percent is reached
//            myRelais.get(myRelais.size() - 1).setState(PinState.HIGH);

        } else {
            for (int relay = 0; relay < myRelais.size(); relay++) {
                boolean on = percent.compareTo(BigDecimal.ZERO) > 0 && relaynum == relay;
                myRelais.get(relay).blink(on ? pulsetimeinmillis : 0);

            }
        }
    }
}
