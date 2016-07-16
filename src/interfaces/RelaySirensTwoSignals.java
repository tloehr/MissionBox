package interfaces;

import main.MissionBox;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by tloehr on 09.07.16.
 */
public class RelaySirensTwoSignals implements PercentageInterface {
    protected final Logger logger = Logger.getLogger(getClass());
//    private final String key1, key2;
    private int previousTenth = -1;
    private long MAXTIMEFORSIGNAL = 5000;

    public RelaySirensTwoSignals(String key1, String key2) {
//        this.key1 = key1;
//        this.key2 = key2;
    }

    @Override
    public void setValue(BigDecimal percent) {

//        if (percent.compareTo(BigDecimal.ZERO) < 0) {
//            MissionBox.blink(key1, 0);
//            MissionBox.blink(key2, 0);
//            previousTenth = -1;
//            return;
//        }
//
//        int tenth = percent.setScale(-1, RoundingMode.HALF_UP).intValue(); // Scale to the power of tenth
//
//        if (tenth == previousTenth) return;
//        previousTenth = tenth;
//
//        logger.debug("tenth " + tenth);
//        String key = "";
//
//        long onTime = -1, offTime = -1;
//        if (tenth < 10) {
//            onTime = 200;
//            key = key1;
//        } else if (tenth <= 90) {
//            onTime =  MAXTIMEFORSIGNAL / 100 * tenth; // new BigDecimal(MAXTIMEFORSIGNAL).divide(BigDecimal.TEN, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(tenth)).longValue();
//            key = key1;
//        } else {
//            onTime = MAXTIMEFORSIGNAL;
//            key = key2;
//        }
//
//        offTime = MAXTIMEFORSIGNAL - onTime;
//        logger.debug("key " + key);
//        logger.debug("onTime " + onTime);
//        logger.debug("offTime " + offTime);


        // MissionBox.blink(key, onTime, offTime, Integer.MAX_VALUE); // only when necessary


    }

}
