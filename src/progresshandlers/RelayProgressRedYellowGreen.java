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
    protected int previousPos = -1;

    final String off = "0;";
    final String on = "1;" + Long.MAX_VALUE + ",0";
    final String slow = Integer.toString(Integer.MAX_VALUE) + ";500,500";
    final String fast = Integer.toString(Integer.MAX_VALUE) + ";100,100";

    //    String[] colors;
    // immer drei angaben, ergeben das blickschma für rot-gelb-grün
    // list geht von links nach rechts, also rot nach grün
    final String[] schemesRed = {fast, slow, slow, off, off, off, off, off, off};
    final String[] schemesYellow = {off, off, slow, slow, on, slow, slow, off, off};
    final String[] schemesGreen = {off, off, off, off, off, off, slow, slow, on};

    public RelayProgressRedYellowGreen(String pinRed, String pinYellow, String pinGreen) {
        super("");
        logger.setLevel(MissionBox.getLogLevel());
        this.pinRed = pinRed;
        this.pinGreen = pinGreen;
        this.pinYellow = pinYellow;
//        colors = new String[]{pinRed, pinRed, pinRed, pinYellow, pinYellow, pinYellow, pinGreen, pinGreen, pinGreen};
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
        int schemepos = new BigDecimal(schemesRed.length-1).divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP).multiply(myPercent).intValue();

        // shortcut
        if (previousPos == schemepos) return;
        previousPos = schemepos;

//        MissionBox.off(pinRed);
//        MissionBox.off(pinGreen);
//        MissionBox.off(pinYellow);

        logger.debug("schemepos " + schemepos);
        logger.debug("schemesRed.length " + schemesRed.length);

        MissionBox.setScheme(pinRed, schemesRed[schemepos]);
        MissionBox.setScheme(pinYellow, schemesYellow[schemepos]);
        MissionBox.setScheme(pinGreen, schemesGreen[schemepos]);

    }

}
