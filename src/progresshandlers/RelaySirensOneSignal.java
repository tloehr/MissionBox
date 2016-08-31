package progresshandlers;

import interfaces.PercentageInterface;
import main.MissionBox;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;

/**
 * Created by tloehr on 09.07.16.
 */
public class RelaySirensOneSignal extends PercentageInterface {
    protected final Logger logger = Logger.getLogger(getClass());
    private final String key;
    private int previousTenth = -1;
    private long MAXTIMEFORSIGNAL = 5000;

    public RelaySirensOneSignal(String key) {
        super("One Signal");
        this.key = key;
    }

    @Override
    public void setValue(BigDecimal percent) {


        if (percent.compareTo(BigDecimal.ZERO) < 0) {
            MissionBox.off(key);
            previousTenth = -1;
            return;
        }


        int tenth = percent.setScale(-1, RoundingMode.HALF_UP).intValue(); // Scale to the power of tenth

        if (tenth == previousTenth) return;
        previousTenth = tenth;


        logger.debug("tenth " + tenth);

        long onTime = -1, offTime = -1;
        if (tenth < 10) {
            onTime = 200;
        } else {
            onTime = MAXTIMEFORSIGNAL / 100 * tenth;
        }

        offTime = MAXTIMEFORSIGNAL - onTime;

        logger.debug("onTime " + onTime);
        logger.debug("offTime " + offTime);


//        MissionBox.blink(key, onTime, offTime, Integer.MAX_VALUE); // only when necessary


    }

}
