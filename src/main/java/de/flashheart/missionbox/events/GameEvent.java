package de.flashheart.missionbox.events;


import de.flashheart.missionbox.misc.Tools;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.EventObject;


/**
 * Created by tloehr on 25.04.15.
 */
public class GameEvent extends EventObject {

    protected DateTime pit;
    protected String event;
    protected int matchid;
    protected long gametime;
    protected long remaining;

    public GameEvent(Object source, String event, int matchid, long gametime, long remaining) {
        this(source, new DateTime(), event, matchid, gametime, remaining);
    }


    public GameEvent(Object source, DateTime pit, String event, int matchid, long gametime, long remaining) {
        super(source);
        this.pit = pit;
        this.event = event;
        this.matchid = matchid;
        this.gametime = gametime;
        this.remaining = remaining;
    }

    public DateTime getPit() {
        return pit;
    }


    public String getEvent() {
        return event;
    }

    public int getMatchid() {
        return matchid;
    }

    public long getGametime() {
        return gametime;
    }

    public long getRemaining() {
        return remaining;
    }

    @Override
    public String toString() {
        return "GameEvent{" +
                "pit=" + pit.toString(DateTimeFormat.mediumDateTime()) +
                ", event=" + event +
                '}';
    }

    public String toPHPArray() {
        return "   ['pit' => '" + pit.toString("HH:mm:ss") + "','event' => '" + event + "','remaining' => '" + Tools.formatLongTime(remaining, "HH:mm:ss") + "'],\n";
    }




}
