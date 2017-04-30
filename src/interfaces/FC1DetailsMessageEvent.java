package interfaces;

/**
 * Dieses Event beinhaltet jedes Detail eines Farcry1 Zeit Ereignisses
 */
public class FC1DetailsMessageEvent extends MessageEvent {

    private long starttime = -1l;
    private long gametimer = 0l; // wie lange läuft das Spiel schon ?
    private long timeWhenTheFlagWasActivated = -1l; // wann wurde die Flagge zuletzt aktiviert. -1l heisst, nicht aktiv.
    private long maxgametime = 0l; // wie lange kann dieses Spiel maximal laufen
    private long capturetime = 0l; // wie lange muss die Flagge gehalten werden bis sie erobert wurde ?

    private long pausingSince = -1l;
    private long resumingSince = -1l;

    public FC1DetailsMessageEvent(Object source, int mode, long starttime, long gametimer, long timeWhenTheFlagWasActivated , long maxgametime, long capturetime, long pausingSince, long resumingSince) {
        super(source, mode);

        this.starttime = starttime;
        this.gametimer = gametimer;
        this.timeWhenTheFlagWasActivated = timeWhenTheFlagWasActivated;
        this.maxgametime = maxgametime;
        this.capturetime = capturetime;
        this.pausingSince = pausingSince;
        this.resumingSince = resumingSince;
    }

    @Override
    public String toString() {
        return "FC1DetailsMessageEvent{" +
                "starttime=" + starttime +
                ", gametimer=" + gametimer +
                ", timeWhenTheFlagWasActivated=" + timeWhenTheFlagWasActivated +
                ", maxgametime=" + maxgametime +
                ", capturetime=" + capturetime +
                ", pausingSince=" + pausingSince +
                ", resumingSince=" + resumingSince +
                "} " + super.toString();
    }
}
