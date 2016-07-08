package interfaces;

import main.MissionBox;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * This class progresses through a group of pins (defined by their keys within the outputMap. According to the number of defined pins
 * and the current percentage set by setValue(), the corresponding pin is chosen to blink in the frequency pulsetimeinmillis.
 */
public class RelaySirenEscalating implements PercentageInterface {
    private final String key;
    protected final Logger logger = Logger.getLogger(getClass());
    final BigDecimal bd5000 = new BigDecimal(5000);
    BigDecimal lastPercentUsed = null;

    /**
     * @param key
     */
    public RelaySirenEscalating(String key) {
        logger.setLevel(MissionBox.getLogLevel());
        this.key = key;
        MissionBox.blink(key, 0);
    }


    public void setValue(BigDecimal percent) {
        long iPercent = percent.intValue();
        logger.debug("percent " + percent);
        logger.debug("ipercent " + iPercent);


        BigDecimal mypercent = percent.divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP);
        logger.debug(mypercent + "%");

        if (iPercent < 0) {
            MissionBox.blink(key, 0);
            return;
        }


        if (mypercent.equals(lastPercentUsed)) {
            return;
        }
        lastPercentUsed = mypercent;

        BigDecimal onTime = bd5000.multiply(mypercent);
        BigDecimal offTime = bd5000.subtract(bd5000.multiply(mypercent));
        logger.debug("onTime " + onTime.longValue());
        logger.debug("offTime " + offTime.longValue());

        if (percent.equals(BigDecimal.ZERO)) {
            // giving the siren a kickstart
            onTime = bd5000.multiply(new BigDecimal(0.01d));
            offTime = bd5000.subtract(bd5000.multiply(new BigDecimal(0.01d)));
            MissionBox.blink(key, 0);
            MissionBox.blink(key, onTime.longValue(), offTime.longValue(), Integer.MAX_VALUE);
        } else if (iPercent < 90) {
            if (iPercent % 10 == 0) {
                MissionBox.blink(key, 0);
                MissionBox.blink(key, onTime.longValue(), offTime.longValue(), Integer.MAX_VALUE);
            }
        } else { // the last to percent have a finer escalation
            if (iPercent % 2 == 0) {
                MissionBox.blink(key, 0);
                MissionBox.blink(key, onTime.longValue(), offTime.longValue(), Integer.MAX_VALUE);
            }
        }
    }
}
