package gamemodes;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Duration;

/**
 * Created by Torsten on 05.07.2016.
 */
public class Farcry1Undo {
    int pmode;
    DateTime flagactivation;
    DateTime endtime;
    DateTime starttime;
    boolean applyUndo;
    DateTime initPointInTime;
    Duration difference;
    Logger logger;

    public Farcry1Undo(int pmode, DateTime starttime, DateTime flagactivation, DateTime endtime) {
        this.logger = Logger.getLogger(this.getClass());
        this.pmode = pmode;
        this.starttime = starttime;
        this.flagactivation = flagactivation;
        this.endtime = endtime;
        this.applyUndo = false;
        this.initPointInTime = new DateTime();
    }

    public void adaptToCurrentTime() {
        difference = new Duration(initPointInTime, new DateTime());
    }

    public int getGameState() {
        return pmode;
    }

    public DateTime getFlagActivation() {
        logger.debug("old flagactivation time " + flagactivation.toString());
        logger.debug("difference " + difference.toString());
        logger.debug("new flagactivation time " + flagactivation.plus(difference).toString());
        return flagactivation.plus(difference);

    }


    public DateTime getEndTime() {
        return endtime.plus(difference);
    }

    public DateTime getStartTime() {

        logger.debug("old start time " + starttime.toString());
        logger.debug("difference " + difference.toString());
        logger.debug("new start time " + starttime.plus(difference).toString());

        return starttime.plus(difference);
    }

    @Override
    public String toString() {
        return "Farcry1Undo{" +
                "pmode=" + pmode +
                ", flagactivation=" + flagactivation +
                ", endtime=" + endtime +
                ", starttime=" + starttime +
                ", initPointInTime=" + initPointInTime +
                ", difference=" + difference +
                '}';
    }
}
