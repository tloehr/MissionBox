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

    /**
     * @param key
     */
    public RelaySirenEscalating(String key) {
        logger.setLevel(MissionBox.getLogLevel());
        this.key = key;
        MissionBox.blink(key, 0);
    }


    public void setValue(BigDecimal percent) {
        logger.debug(percent + "%");

        long iPercent = percent.intValue();

        percent = percent.divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP);

        if (iPercent < 0) {
            MissionBox.blink(key, 0);
            return;
        }

        if (iPercent < 90){
            if (iPercent % 10 == 0){
                logger.debug("onTime "+ bd5000.multiply(percent));
                logger.debug("offTime "+bd5000.subtract(bd5000.multiply(percent)));
                // MissionBox.blink(key, bd5000.multiply(percent).longValue(), bd5000.subtract(bd5000.multiply(percent)).longValue(), Integer.MAX_VALUE);
            }
        } else { // the last to percent have a finer escalation
            if (iPercent % 2 == 0){
                logger.debug("onTime "+bd5000.multiply(percent));
                logger.debug("offTime "+bd5000.subtract(bd5000.multiply(percent)));
//                MissionBox.blink(key, bd5000.multiply(percent).longValue(), bd5000.subtract(bd5000.multiply(percent)).longValue(), Integer.MAX_VALUE);
            }
        }
    }
}
