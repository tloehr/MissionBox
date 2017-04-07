package gamemodes;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

/**
 * Created by Torsten on 05.07.2016.
 */
public class Farcry1GameEvent {
    private final long maxgametime;
    private final long capturetime;
    private int gameState;
    private long endtime;
    // wann hat dieses Ereignis stattgefunden ?
    private long eventTime;
    private long gametimer;
    private Logger logger;


    public Farcry1GameEvent(int gameState, long gametimer, long maxgametime, long capturetime) {
        this.maxgametime = maxgametime;
        this.capturetime = capturetime;
        this.gameState = gameState;
        this.eventTime = System.currentTimeMillis();
        this.gametimer = gametimer;
    }

    public int getGameState() {
        return gameState;
    }

    public long getEventTime() {
        return eventTime;
    }

    public long getGametimer() {
        return gametimer;
    }

    public long getEndtime() {
        long endtime = maxgametime;
        if (gameState == Farcry1AssaultThread.GAME_FLAG_HOT) {
            endtime = gametimer + capturetime;
        }
        return endtime;
    }

    //    public DateTime getNewFlagactivation(DateTime) {
//        Duration difference = new Duration(eventTime, new DateTime());
//        logger.debug("old flagactivation time " + flagactivation.toString());
//        logger.debug("difference " + difference.toString());
//        logger.debug("new flagactivation time " + flagactivation.plus(difference).toString());
//
//        return flagactivation.plus(difference);
//
//    }
//
//
//    public DateTime getEndtime() {
//        Duration difference = new Duration(eventTime, new DateTime());
//        return endtime.plus(difference);
//    }
//
//    public DateTime getStarttime() {
//        Duration difference = new Duration(eventTime, new DateTime());
//        logger.debug("old start time " + starttime.toString());
//        logger.debug("difference " + difference.toString());
//        logger.debug("new start time " + starttime.plus(difference).toString());
//
//        return starttime.plus(difference);
//    }

    @Override
    public String toString() {

        String html = "<b>" + new DateTime(eventTime).toString(DateTimeFormat.mediumTime()) + "</b>&nbsp;";
        html += Farcry1AssaultThread.GAME_MODES[gameState];

        return "<html>" + html + "</html>";

//        return "Farcry1GameEvent{" +
//                "gameState=" + Farcry1AssaultThread.GAME_MODES[gameState] +
//                ", flagactivation=" + flagactivation +
//                ", eventTime=" + eventTime +
//                '}';
    }
}
