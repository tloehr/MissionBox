package gamemodes;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

/**
 * Created by Torsten on 05.07.2016.
 */
public class Farcry1GameEvent {
    int pmode;
    DateTime flagactivation;
    DateTime endtime;
    DateTime starttime;
    DateTime eventTime;
    Logger logger;


    public Farcry1GameEvent(int pmode) {
        this.logger = Logger.getLogger(this.getClass());
        this.pmode = pmode;
        this.eventTime = new DateTime();
    }

//    public void finalizeInit(DateTime starttime, DateTime flagactivation, DateTime endtime){
//        this.starttime = starttime;
//        this.flagactivation = flagactivation;
//        this.endtime = endtime;
//        this.initPointInTime = new DateTime();
//    }


    public int getGameState() {
        return pmode;
    }

    public DateTime getFlagactivation() {
        Duration difference = new Duration(eventTime, new DateTime());
        logger.debug("old flagactivation time " + flagactivation.toString());
        logger.debug("difference " + difference.toString());
        logger.debug("new flagactivation time " + flagactivation.plus(difference).toString());

        return flagactivation.plus(difference);

    }


    public DateTime getEndtime() {
        Duration difference = new Duration(eventTime, new DateTime());
        return endtime.plus(difference);
    }

    public DateTime getStarttime() {
        Duration difference = new Duration(eventTime, new DateTime());
        logger.debug("old start time " + starttime.toString());
        logger.debug("difference " + difference.toString());
        logger.debug("new start time " + starttime.plus(difference).toString());

        return starttime.plus(difference);
    }

    @Override
    public String toString() {

        String html = "<b>"+eventTime.toString(DateTimeFormat.fullTime())+"</b>&nbsp;";
        html += Farcry1AssaultThread.GAME_MODES[pmode];

        return "<html>"+html+"</html>";

//        return "Farcry1GameEvent{" +
//                "pmode=" + Farcry1AssaultThread.GAME_MODES[pmode] +
//                ", flagactivation=" + flagactivation +
//                ", eventTime=" + eventTime +
//                '}';
    }
}
