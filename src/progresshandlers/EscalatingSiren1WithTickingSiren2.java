package progresshandlers;

import interfaces.PercentageInterface;
import main.MissionBox;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.math.BigDecimal;


public class EscalatingSiren1WithTickingSiren2 extends PercentageInterface {
    //    long pulsetimeinmillis = 1000; // Integer.parseInt(MissionBox.getConfig().getProperty(MissionBox.MBX_SIREN_TIME));
    private int previousTenth = -1;
    protected final Logger logger = Logger.getLogger(getClass());
    protected final String mainSiren;
    private final String tickingSiren;
    private boolean siren1running = false;

//    private String FOREVER = Integer.toString(Integer.MAX_VALUE);
//    private long MAXTIMEFORSIGNAL = 5000;

//    int[] progress


    /**
     * Signal über Sirene 1. Mit gleichbleibendem Signal Schema.
     * Sirene 2 tickt quasi im Hintergrund. Und mit zunehmendem Fortschritt immer intensiver.
     *
     * @param mainSiren
     * @param tickingSiren
     */
    public EscalatingSiren1WithTickingSiren2(String mainSiren, String tickingSiren) {
        super("Siren1, escalating Siren2");
        this.mainSiren = mainSiren;
        this.tickingSiren = tickingSiren;
        logger.setLevel(MissionBox.getLogLevel());
    }

    /**
     * @param percent Fortschrittsangabe wie weit wir auf dem Weg zur Erlangung der Flagge sind. <0 und >100 heisst die Sirenen werden abgeschaltet.
     */
    public void setValue(BigDecimal percent) {
        logger.debug("PERCENT: " + percent);

        if (percent.compareTo(BigDecimal.ZERO) < 0 || percent.compareTo(new BigDecimal(100)) >= 0) {
            MissionBox.off(mainSiren);
            MissionBox.off(tickingSiren);
            previousTenth = -1;
            siren1running = false;
            return;
        } else {
            // Die Siren1 muss nur einmal aktiviert werden. Sie bleibt an bis zum Schluss.
            if (!siren1running) {
                MissionBox.setScheme(mainSiren, "1000;70,25,70,25,70,25,800,5000");
                siren1running = true;
            }
        }

        // verhindert, dass die Sirene zu oft verändert wird.
        int tenth = new BigDecimal(percent.intValue() / 10).multiply(BigDecimal.TEN).intValue();

        if (tenth == previousTenth) return;
        previousTenth = tenth;

        String tickingPattern = StringUtils.repeat("50,75,", tenth / 10);
        tickingPattern += "50,1000"; // der letzte hat eine lange Pause
        logger.debug("tenth: " + tenth);

        MissionBox.setScheme(tickingSiren, String.format("500;%s", tickingPattern));
    }
}
