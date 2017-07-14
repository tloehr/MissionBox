package progresshandlers;

import interfaces.PercentageInterface;
import main.MissionBox;
import org.apache.log4j.Logger;
import org.joda.time.Interval;
import org.joda.time.Seconds;

import java.math.BigDecimal;

/**
 * This class progresses through a group of pins (defined by their keys within the outputMap. According to the number of defined pins
 * and the current percentage set by setValue(), the corresponding pin is chosen to blink in the frequency pulsetimeinmillis.
 */
public class TickingSlowAndSilent extends PercentageInterface {
    private int previousThird = -1;
    private final Logger logger = Logger.getLogger(getClass());
    private final String key;
//    private boolean outroProcedureRunning;


    /**
     * @param siren
     */
    public TickingSlowAndSilent(String siren) {
        super("Ticking Slow and Silent");
        this.key = siren;
//        outroProcedureRunning = false;
        logger.setLevel(MissionBox.getLogLevel());
    }


    public void setValue(BigDecimal percent) {
        if (percent.compareTo(BigDecimal.ZERO) < 0 || percent.compareTo(new BigDecimal(100)) >= 0) {
            MissionBox.off(key);
            previousThird = -1;
//            outroProcedureRunning = false;
            return;
        }

        // sind wir in den letzten 10 sekunden ?
 // dann brauchen wir keinen progress mehr. 
        Interval remaining = new Interval(now, end);
        if (Seconds.secondsIn(remaining).getSeconds() <= 10) return;
//        {
////            outroProcedureRunning = true;
//            MissionBox.setScheme(key, "10;500,500");
//        }

        // Fortschritt im ersten Drittel.
        int third = -1;
        if (percent.compareTo(new BigDecimal(33)) <= 0) {
            third = 1;
        } else if (percent.compareTo(new BigDecimal(33)) > 0 && (percent.compareTo(new BigDecimal(66)) <= 0)) {
            third = 2;
        } else {
            third = 3;
        }


        if (third == previousThird) return;
        previousThird = third;

        String tickingScheme = "";

        // intro signal zu beginn jedes drittels
        tickingScheme = "1;70,25,70,25,70,25,800,75,";

        if (third == 1) {
            for (int t = 0; t < 1000; t++) {
                tickingScheme += "100,10000,";
            }
        } else if (third == 2) {
            for (int t = 0; t < 1000; t++) {
                tickingScheme += "100,100,100,10000,";
            }
        } else {
            for (int t = 0; t < 1000; t++) {
                tickingScheme += "100,100,100,100,100,10000,";
            }
        }

        MissionBox.setScheme(key, tickingScheme);


        // hundertmal sollten oft genug sein


    }
}