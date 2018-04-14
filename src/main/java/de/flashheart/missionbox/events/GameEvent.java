package de.flashheart.missionbox.events;


import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.EventObject;


/**
 * Created by tloehr on 25.04.15.
 */
public class GameEvent extends EventObject {

    protected DateTime pit;
    protected String event;

    public GameEvent(Object source, String event) {
        this(source, new DateTime(), event);
    }


    public GameEvent(Object source, DateTime pit, String event) {
        super(source);
        this.pit = pit;
        this.event = event;
    }

    public DateTime getPit() {
        return pit;
    }


    public String getEvent() {
        return event;
    }

    @Override
    public String toString() {
        return "GameEvent{" +
                "pit=" + pit.toString(DateTimeFormat.mediumDateTime()) +
                ", event=" + event +
                '}';
    }

    public String toPHPArray() {
        return "   ['pit' => '" + pit.toString("HH:mm:ss") + "','event' => '" + event + "'],\n";
    }


}
