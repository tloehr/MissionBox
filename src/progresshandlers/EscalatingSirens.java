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
public class EscalatingSirens extends PercentageInterface {

    protected final List<String> keys;
    protected final Logger logger = Logger.getLogger(getClass());
    protected int previousRelay = -1;

    /**
     * please note that all the pins have to be within the same CD in order for this Interface to run properly.
     * @param myKeys
     */
    public EscalatingSirens(String... myKeys) {
        super("Escalating over Sirens");
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



        // as we use the PinHandler, and all the sirens are within the same CD, we only need to set ONE pin to off. The others are handled automatically.
        if (relaynum >= keys.size()) {



//            for (String key : keys) {
//                MissionBox.off(key);
//            }


            MissionBox.off(keys.get(0));


        } else {
            for (String key : keys) {
                if (myPercent.compareTo(BigDecimal.ZERO) > 0 && relaynum == keys.indexOf(key)) {
                    MissionBox.on(key);
                }
            }
        }

    }
}
