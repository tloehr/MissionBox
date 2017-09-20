package progresshandlers;

import interfaces.PercentageInterface;
import main.MissionBox;
import org.apache.log4j.Logger;

import java.math.BigDecimal;

/**
 * Created by tloehr on 07.06.15.
 */
public class RelayProgressRedYellowGreen extends PercentageInterface {

    protected final Logger logger = Logger.getLogger(getClass());
    private final String pinRed;
    private final String pinGreen;
    private final String pinYellow;
//    private String prevRed = "", prevGreen = "", prevYellow = "";
    protected int previousPos = -1;

    final String off = "0;";
    final String onn = "1;" + Long.MAX_VALUE + ",0";
    final String slo = Integer.toString(Integer.MAX_VALUE) + ";750,750";
    final String fst = Integer.toString(Integer.MAX_VALUE) + ";200,200";
    final String vfs = Integer.toString(Integer.MAX_VALUE) + ";100,100"; // very fast
    final String sfs = Integer.toString(Integer.MAX_VALUE) + ";50,50"; // super very fast

    final String[] schemesRedXXX = {off, off, fst, vfs};
    final String[] schemesYellow = {off, fst, off, off};
    final String[] schemesGreenX = {slo, off, off, off};


    public RelayProgressRedYellowGreen(String pinRed, String pinYellow, String pinGreen) {
        super("");
        logger.setLevel(MissionBox.getLogLevel());
        this.pinRed = pinRed;
        this.pinGreen = pinGreen;
        this.pinYellow = pinYellow;
        previousPos = -1;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    public void setValue(BigDecimal percent) {

        if (percent.equals(BigDecimal.ONE.negate())){
            previousPos = -1;
            MissionBox.off(pinRed);
            MissionBox.off(pinYellow);
            MissionBox.off(pinGreen);
            return;
        }

        BigDecimal bdPos = new BigDecimal(schemesRedXXX.length).divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP).multiply(percent);

        int intpos = bdPos.setScale(0, BigDecimal.ROUND_DOWN).intValue();

        logger.debug("Percent: " + percent.toPlainString() + ", #schemes: " + schemesRedXXX.length + ", bdpos: " + bdPos +", intpos: " + intpos+", previousPos: "+previousPos);

        // shortcut
        if (previousPos == intpos) {
//            logger.debug("shortcutting...");
            return;
        }
        previousPos = intpos;

        logger.debug("intpos " + intpos);
        logger.debug("schemesRedXXX.length " + schemesRedXXX.length);

//        // die IFs nur, damit die LEDs gleichmässig blinken und nicht immer aussetzer haben
//        if (!prevRed.equals(schemesRedXXX[schemepos])) {
//            prevRed = schemesRedXXX[schemepos];
//            MissionBox.setScheme(pinRed, schemesRedXXX[schemepos]);
//        }
//        if (!prevGreen.equals(schemesGreenX[schemepos])) {
//            prevGreen = schemesGreenX[schemepos];
//            MissionBox.setScheme(pinGreen, schemesGreenX[schemepos]);
//        }
//        if (!prevYellow.equals(schemesYellow[schemepos])) {
//            prevYellow = schemesYellow[schemepos];
//            MissionBox.setScheme(pinYellow, schemesYellow[schemepos]);
//        }

        MissionBox.setScheme(pinRed, schemesRedXXX[intpos]);
        MissionBox.setScheme(pinYellow, schemesYellow[intpos]);
        MissionBox.setScheme(pinGreen, schemesGreenX[intpos]);

    }

}
