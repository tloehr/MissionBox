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
    final String slo = Integer.toString(Integer.MAX_VALUE) + ";750,750";
    final String fst = Integer.toString(Integer.MAX_VALUE) + ";200,200";
    final String vfs = Integer.toString(Integer.MAX_VALUE) + ";100,100"; // very fast

    //    String[] colors;
    // immer drei angaben, ergeben das blickschma für rot-gelb-grün
    // list geht von links nach rechts, also rot nach grün
    //                              RED           YELLOW                   GREEN
//    final String[] schemesRedXXX = { vfs, fst, slo, onn, slo, off, off, off, off, off};
//    final String[] schemesYellow = { off, off, off, off, slo, onn, slo, slo, off, off};
//    final String[] schemesGreenX = { off, off, off, off, off, off, off, slo, slo, onn};
//
//    final String[] schemesRedXXX = { vfs, fst, slo, slo, off, off, off};
//    final String[] schemesYellow = { off, off, off, slo, slo, slo, off};
//    final String[] schemesGreenX = { off, off, off, off, off, slo, slo};

    final String[] schemesRedXXX = {off, off, off, slo, slo, fst, vfs};
    final String[] schemesYellow = {off, slo, slo, slo, off, off, off};
    final String[] schemesGreenX = {slo, slo, off, off, off, off, off};


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
//        BigDecimal myPercent = new BigDecimal(100).subtract(percent);

        BigDecimal bdPos = new BigDecimal(schemesRedXXX.length).divide(new BigDecimal(100), 0, BigDecimal.ROUND_HALF_UP).multiply(percent);
        int intpos = bdPos.intValue();


        logger.debug("Percent: " + percent.toPlainString() + ", #schemes: " + schemesRedXXX.length + ", intpos: " + intpos);

        // shortcut
        if (previousPos == intpos) return;
        previousPos = intpos;

//        MissionBox.off(pinRed);
//        MissionBox.off(pinGreen);
//        MissionBox.off(pinYellow);

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
