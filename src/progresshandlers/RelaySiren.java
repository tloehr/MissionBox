package progresshandlers;

import interfaces.PercentageInterface;
import main.MissionBox;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Created by tloehr on 07.06.15.
 */
public class RelaySiren extends PercentageInterface {

    protected final List<String> keys;
    protected final Logger logger = Logger.getLogger(getClass());
    protected int previousRelay = -1;

    public RelaySiren(String... myKeys) {
        super("Sirens Escalating");
        keys = Arrays.asList((String[]) myKeys);
    }


    @Override
    public void setValue(BigDecimal percent) {

        logger.debug("PERCENT: " + percent);

        if (percent.compareTo(BigDecimal.ZERO) < 0) {
            for (String key : keys) {
                MissionBox.off(key);
            }
            return;
        }

        BigDecimal myPercent = new BigDecimal(100).subtract(percent);
        int relaynum = new BigDecimal(keys.size()).divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP).multiply(myPercent).intValue();

        // shortcut
        if (previousRelay == relaynum) return;
        previousRelay = relaynum;


        if (relaynum >= keys.size()) {
            for (String key : keys) {
                MissionBox.off(key);
            }
        } else {
            for (String key : keys) {
                if (myPercent.compareTo(BigDecimal.ZERO) > 0 && relaynum == keys.indexOf(key)) {
                    MissionBox.on(key);
                } else {
                    MissionBox.off(key);
                }
            }
        }

    }
}
