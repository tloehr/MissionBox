package gamemodes;

import interfaces.Undoable;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;

/**
 * Created by Torsten on 05.07.2016.
 */
public class Farcry1Undo {
    int pmode;
    DateTime progress;
    DateTime endtime;
    DateTime starttime;
    boolean applyUndo;
    DateTime initPointInTime;
    Duration difference;

    public Farcry1Undo(int pmode, DateTime starttime, DateTime progress, DateTime endtime) {
        this.pmode = pmode;
        this.starttime = starttime;
        this.progress = progress;
        this.endtime = endtime;
        this.applyUndo = false;
        this.initPointInTime = new DateTime();
    }

    private void adaptToCurrentTime() {
        difference = new Duration(initPointInTime, new DateTime());
    }


    public int getGameState() {
        return pmode;
    }

    public boolean isApplyUndo() {
        return applyUndo && starttime != null;
    }

    public void setApplyUndo(boolean applyUndo) {
        if (applyUndo) adaptToCurrentTime();
        this.applyUndo = applyUndo;
    }

    public DateTime getProgressTime() {
        return progress.plus(difference);
    }


    public DateTime getEndTime() {
        return endtime.plus(difference);
    }

    public DateTime getStartTime() {
        return starttime.plus(difference);
    }
}
