package interfaces;

import main.MissionBox;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Created by tloehr on 07.06.15.
 */
public class RelaySiren implements PercentageInterface {

    protected final ArrayList<Relay> myRelais;
    protected long lastChangeTime;
    protected final Logger logger = Logger.getLogger(getClass());

    public RelaySiren(ArrayList<Relay> myRelais) {
        this.myRelais = myRelais;
        logger.setLevel(MissionBox.getLogLevel());
        for (Relay pin : myRelais) {
            pin.setOn(false);
        }
    }


    @Override
    public void setValue(BigDecimal percent) {
        if (percent.compareTo(BigDecimal.ZERO) < 0) {
            for (int relay = 0; relay < myRelais.size(); relay++) {
                myRelais.get(relay).setOn(false);
            }
            return;
        }

        BigDecimal myPercent = new BigDecimal(100).subtract(percent);

        lastChangeTime = System.currentTimeMillis();
        int relaynum = new BigDecimal(myRelais.size()).divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP).multiply(myPercent).intValue();

        if (relaynum >= myRelais.size()) {
            for (int relay = 0; relay < myRelais.size(); relay++) {
                myRelais.get(relay).setOn(false);
            }
        } else {
            for (int relay = 0; relay < myRelais.size(); relay++) {
                myRelais.get(relay).setOn(myPercent.compareTo(BigDecimal.ZERO) > 0 && relaynum == relay);
            }
        }

    }
}
