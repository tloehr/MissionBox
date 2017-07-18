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
    private String prevRed = "", prevGreen = "", prevYellow = "";
    protected int previousPos = -1;

    final String off = "0;";
    final String onn = "1;" + Long.MAX_VALUE + ",0";
    final String slo = Integer.toString(Integer.MAX_VALUE) + ";500,500";
    final String fst = Integer.toString(Integer.MAX_VALUE) + ";100,100";
    final String vfs = Integer.toString(Integer.MAX_VALUE) + ";50,50"; // very fast

    //    String[] colors;
    // immer drei angaben, ergeben das blickschma für rot-gelb-grün
    // list geht von links nach rechts, also rot nach grün
    //                              RED           YELLOW                   GREEN
    final String[] schemesRedXXX = {vfs, fst, slo, slo, off, off, off, off};
    final String[] schemesYellow = {vfs, off, off, slo, slo, slo, off, off};
    final String[] schemesGreenX = {vfs, off, off, off, off, slo, slo, onn};

    public RelayProgressRedYellowGreen(String pinRed, String pinYellow, String pinGreen) {
        super("");
        logger.setLevel(MissionBox.getLogLevel());
        this.pinRed = pinRed;
        this.pinGreen = pinGreen;
        this.pinYellow = pinYellow;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    public void setValue(BigDecimal percent) {

//        if (percent.compareTo(BigDecimal.ZERO) < 0) {
//            MissionBox.off(pinRed);
//            MissionBox.off(pinGreen);
//            MissionBox.off(pinYellow);
//            return;
//        }

        // /3 weil immer drei werte in der Liste zusammengehören
        BigDecimal myPercent = new BigDecimal(100).subtract(percent);
        int schemepos = new BigDecimal(schemesRedXXX.length - 1).divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP).multiply(myPercent).intValue();

        // shortcut
        if (previousPos == schemepos) return;
        previousPos = schemepos;

//        MissionBox.off(pinRed);
//        MissionBox.off(pinGreen);
//        MissionBox.off(pinYellow);

        logger.debug("schemepos " + schemepos);
        logger.debug("schemesRedXXX.length " + schemesRedXXX.length);

        // die IFs nur, damit die LEDs gleichmässig blinken und nicht immer aussetzer haben
        if (!prevRed.equals(schemesRedXXX[schemepos])) {
            prevRed = schemesRedXXX[schemepos];
            MissionBox.setScheme(pinRed, schemesRedXXX[schemepos]);
        }
        if (!prevGreen.equals(schemesGreenX[schemepos])) {
            prevGreen = schemesGreenX[schemepos];
            MissionBox.setScheme(pinGreen, schemesGreenX[schemepos]);
        }
        if (!prevYellow.equals(schemesYellow[schemepos])) {
            prevYellow = schemesYellow[schemepos];
            MissionBox.setScheme(pinYellow, schemesYellow[schemepos]);
        }


        MissionBox.setScheme(pinYellow, schemesYellow[schemepos]);
        MissionBox.setScheme(pinGreen, schemesGreenX[schemepos]);

    }

}
