package progresshandlers;

import interfaces.PercentageInterface;
import main.MissionBox;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

/**
 * Created by tloehr on 07.06.15.
 */
public class EscalatingSirensTime extends PercentageInterface {

    protected final List<String> keys;
    protected final Logger logger = Logger.getLogger(getClass());
    protected int previousRelay = -1;
    private int previousTenth = -1;
    private String FOREVER = Integer.toString(Integer.MAX_VALUE);

    /**
     * please note that all the pins have to be within the same CD in order for this Interface to run properly.
     *
     * @param myKeys
     */
    public EscalatingSirensTime(String... myKeys) {
        super("Escalating over Sirens and Time");
        keys = Arrays.asList((String[]) myKeys);
    }


    @Override
    public void setValue(BigDecimal percent) {

        logger.debug("Progress: "+percent.toString());

        if (percent.compareTo(BigDecimal.ZERO) < 0) {
            // as we use the PinHandler, and all the sirens are within the same CD, we only need to set ONE pin to off. The others are handled automatically.
            MissionBox.off(keys.get(0));
            previousTenth = -1;
            return;
        }

        //calculate Siren NO
//        BigDecimal myPercent = new BigDecimal(100).subtract(percent);
        int relaynum = new BigDecimal(keys.size()).divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP).multiply(percent).intValue();



        // calculate time
        int tenth = percent.setScale(-1, RoundingMode.DOWN).intValue(); // Scale to the power of tenth
        long onTime = 1000;
        long offTime = Math.max(0, ((100 - tenth) - 10) * 100); //-10 because i wanted to increase the siren time. Never less than 0.


        if (previousRelay == relaynum && tenth == previousTenth){
            return;
        }
        previousRelay = relaynum;
        previousTenth = tenth;


        logger.debug("relay:" + relaynum + ", onTime:" + onTime + ", offTime:" + offTime);


        if (relaynum >= keys.size()) {
            MissionBox.off(keys.get(0));
        } else {
            for (String key : keys) {
                if (relaynum == keys.indexOf(key)) {
                    MissionBox.setScheme(key, FOREVER + ";" + onTime + "," + offTime);
                    break;
                }
            }
        }
    }
}
