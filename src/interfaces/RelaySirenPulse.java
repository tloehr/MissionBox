package interfaces;

import com.pi4j.io.gpio.PinState;
import main.MissionBox;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * This class progresses through a group of pins (defined by their keys within the outputMap. According to the number of defined pins
 * and the current percentage set by setValue(), the corresponding pin is chosen to blink in the frequency pulsetimeinmillis.
 */
public class RelaySirenPulse implements PercentageInterface {
    long pulsetimeinmillis = 1000; // Integer.parseInt(MissionBox.getConfig().getProperty(MissionBox.MBX_SIREN_TIME));
    int lastRelay = -1;
    protected final Logger logger = Logger.getLogger(getClass());
    ArrayList<String> myRelaisKeys;


    /**
     *
     * @param myRelaisKeys
     */
    public RelaySirenPulse(ArrayList<String> myRelaisKeys) {
        this.myRelaisKeys = myRelaisKeys;
        logger.setLevel(MissionBox.getLogLevel());
        for (String pin : myRelaisKeys) {
            MissionBox.setState(pin, PinState.LOW);
        }
    }


    public void setValue(BigDecimal percent) {

        if (percent.compareTo(BigDecimal.ZERO) <= 0){
            for (int relay = 0; relay < myRelaisKeys.size(); relay++) {
                MissionBox.blink(myRelaisKeys.get(relay), 0);
            }
            lastRelay = -1;
            return;
        }

        logger.debug(percent+" %");
        BigDecimal bd = new BigDecimal(myRelaisKeys.size()).divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP).multiply(percent);
        logger.debug("bd "+ bd);
        int relaynum = bd.intValue();
        logger.debug("relaynum "+relaynum);
        logger.debug("lastRelay "+lastRelay);
        if (lastRelay == relaynum) return;

        lastRelay = relaynum;

        if (relaynum >= myRelaisKeys.size()) {
            for (int relay = 0; relay < myRelaisKeys.size(); relay++) {
                MissionBox.blink(myRelaisKeys.get(relay), 0);
            }
            // leave the last one blinking, when 100 (or above) percent is reached
            MissionBox.blink(myRelaisKeys.get(myRelaisKeys.size() - 1), pulsetimeinmillis);
        } else {
            for (int relay = 0; relay < myRelaisKeys.size(); relay++) {
                boolean on = percent.compareTo(BigDecimal.ZERO) > 0 && relay == relaynum;

                logger.debug("relaynum " + relaynum);
                logger.debug("relay " + relay);

                logger.debug("on: " + on);
                logger.debug("myRelaisKeys.get(relay): " + myRelaisKeys.get(relay));

                MissionBox.blink(myRelaisKeys.get(relay), (on ? pulsetimeinmillis : 0));
            }
        }
    }
}
