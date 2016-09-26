package progresshandlers;

import interfaces.PercentageInterface;
import main.MissionBox;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * This class progresses through a group of pins (defined by their keys within the outputMap. According to the number of defined pins
 * and the current percentage set by setValue(), the corresponding pin is chosen to blink in the frequency pulsetimeinmillis.
 */
public class EscalatingTime extends PercentageInterface {
    long pulsetimeinmillis = 1000; // Integer.parseInt(MissionBox.getConfig().getProperty(MissionBox.MBX_SIREN_TIME));
    private int previousTenth = -1;
    protected final Logger logger = Logger.getLogger(getClass());
    protected final String key;
    private String FOREVER = Integer.toString(Integer.MAX_VALUE);
    private long MAXTIMEFORSIGNAL = 5000;

    /**
     * @param key
     */
    public EscalatingTime(String key) {
        super("Escalating over Time");
        this.key = key;
    }


    public void setValue(BigDecimal percent) {
        logger.debug("PERCENT: " + percent);

        if (percent.compareTo(BigDecimal.ZERO) < 0) {
            MissionBox.off(key);
            previousTenth = -1;
            return;
        }

        int tenth = percent.setScale(-1, RoundingMode.HALF_UP).intValue(); // Scale to the power of tenth

        if (tenth == previousTenth) return;
        previousTenth = tenth;

        long onTime = -1, offTime = -1;
        if (tenth < 10) {
            onTime = 200;
        } else {
            onTime = MAXTIMEFORSIGNAL / 100 * tenth;
        }

        offTime = MAXTIMEFORSIGNAL - onTime;

        logger.debug("onTime " + onTime);
        logger.debug("offTime " + offTime);

        // MissionBox.setScheme(key, FOREVER + ";" + onTime + "," + offTime);
    }
}
