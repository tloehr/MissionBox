package interfaces;

import org.joda.time.DateTime;

/**
 * Created by Torsten on 05.07.2016.
 */
public interface Undoable {

    public int getPreviousMode();
    public DateTime getProgressTime();

    public DateTime getEndTime();

}
