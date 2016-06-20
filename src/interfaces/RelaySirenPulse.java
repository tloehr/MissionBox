package interfaces;

import com.pi4j.io.gpio.PinState;
import main.MissionBox;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Created by Torsten on 19.06.2016.
 */
public class RelaySirenPulse implements PercentageInterface {
    long pulsetimeinmillis = 1000;
    int lastRelay = -1;
    protected final Logger logger = Logger.getLogger(getClass());
    ArrayList<String> myRelaisKeys;

    public RelaySirenPulse(ArrayList<String> myRelaisKeys, long pulsetimeinmillis) {
        this.pulsetimeinmillis = pulsetimeinmillis;
        this.myRelaisKeys = myRelaisKeys;
        logger.setLevel(MissionBox.getLogLevel());
        for (String pin : myRelaisKeys) {
            MissionBox.setState(pin, PinState.LOW);
        }
    }

    public void setValue(BigDecimal percent) {

        // Warum setzt der so verzögert ein ?

        int relaynum = new BigDecimal(myRelaisKeys.size()).divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP).multiply(percent).intValue() - 1;
        if (lastRelay == relaynum) return;

        logger.debug(percent + " %");
        logger.debug("relaynum " + relaynum + " out of " + myRelaisKeys.size());

        if (relaynum >= myRelaisKeys.size()) {
            for (int relay = 0; relay < myRelaisKeys.size(); relay++) {

                MissionBox.blink(myRelaisKeys.get(relay), 0);
            }
            // leave the last one on, when 100 percent is reached
//            myRelais.get(myRelais.size() - 1).setState(PinState.HIGH);

        } else {
            for (int relay = 0; relay < myRelaisKeys.size(); relay++) {
                boolean on = percent.compareTo(BigDecimal.ZERO) > 0 && relaynum == relay;
//                MissionBox.setState(myRelaisKeys.get(relay), PinState.LOW);
                MissionBox.blink(myRelaisKeys.get(relay), (on ? pulsetimeinmillis : 0));
            }
        }

        lastRelay = relaynum;
    }
}
