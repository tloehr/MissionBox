package de.flashheart.missionbox.progresshandlers;

import de.flashheart.missionbox.Main;

import org.apache.log4j.Logger;

import java.math.BigDecimal;

/**
 * Created by tloehr on 07.06.15.
 */
public class RelayProgressRedYellowGreen extends ProgressInterface {

    protected final Logger logger = Logger.getLogger(getClass());
    private final String pinRed;
    private final String pinGreen;
    private final String pinYellow;
    protected int previousPos = -1;



    // "∞:on,1000;off,∞"


    final String off = "0:";
    final String onn = "1:on,∞;off,0";
    final String slo = "∞:on,750;off,750";
    final String fst = "∞:on,200;off,200";
    final String vfs = "∞:on,100;off,100"; // very fast
    final String sfs = "∞:on,50;off,50"; // super very fast

    final String[] schemesRedXXX = {off, off, fst, vfs};
    final String[] schemesYellow = {off, fst, off, off};
    final String[] schemesGreenX = {slo, off, off, off};


    public RelayProgressRedYellowGreen(String pinRed, String pinYellow, String pinGreen) {
        super("");
        logger.setLevel(Main.getLogLevel());
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
            Main.getPinHandler().off(pinRed);
            Main.getPinHandler().off(pinYellow);
            Main.getPinHandler().off(pinGreen);
            return;
        }

        BigDecimal bdPos = new BigDecimal(schemesRedXXX.length).divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP).multiply(percent);

        int intpos = Math.min(bdPos.setScale(0, BigDecimal.ROUND_DOWN).intValue(), schemesRedXXX.length-1);

        if (previousPos == intpos) {
            return;
        }
        previousPos = intpos;

        Main.getPinHandler().setScheme(pinRed, schemesRedXXX[intpos]);
        Main.getPinHandler().setScheme(pinYellow, schemesYellow[intpos]);
        Main.getPinHandler().setScheme(pinGreen, schemesGreenX[intpos]);

    }

}
