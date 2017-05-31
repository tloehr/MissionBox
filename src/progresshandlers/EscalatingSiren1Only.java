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
public class EscalatingSiren1Only extends PercentageInterface {
    //    long pulsetimeinmillis = 1000; // Integer.parseInt(MissionBox.getConfig().getProperty(MissionBox.MBX_SIREN_TIME));
    private int previousTenth = -1;
    protected final Logger logger = Logger.getLogger(getClass());
    protected final String key;

//    private String FOREVER = Integer.toString(Integer.MAX_VALUE);
//    private long MAXTIMEFORSIGNAL = 5000;


    /**
     * @param key
     */
    public EscalatingSiren1Only(String key) {
        super("Escalating with Siren 1 only");
        this.key = key;
        logger.setLevel(MissionBox.getLogLevel());
    }


    public void setValue(BigDecimal percent) {
//       logger.debug("PERCENT: " + percent);

        if (percent.compareTo(BigDecimal.ZERO) <= 0) {
            MissionBox.off(key);
            previousTenth = -1;
            return;
        }


        int tenth = new BigDecimal(percent.intValue() / 10).multiply(BigDecimal.TEN).intValue();

//        int tenth = percent.setScale(-1, RoundingMode.HALF_UP).intValue(); // Scale to the power of tenth

        if (tenth == previousTenth) return;
        previousTenth = tenth;




        String pause = new Integer((110 - tenth) * 100).toString();

        // hundertmal sollten oft genug sein
        MissionBox.setScheme(key, "100;70,25,70,25,70,25,800," + pause);

    }
}
