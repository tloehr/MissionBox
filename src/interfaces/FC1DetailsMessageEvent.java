package interfaces;

import gamemodes.Farcry1AssaultThread;
import misc.Tools;

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
    private long lastrespawn;
    private long respawninterval;
    private long resumeinterval;

    public long getStarttime() {
        return starttime;
    }

    public long getGametimer() {
        return gametimer;
    }

    public long getTimeWhenTheFlagWasActivated() {
        return timeWhenTheFlagWasActivated;
    }

    public long getMaxgametime() {
        return maxgametime;
    }

    public long getCapturetime() {
        return capturetime;
    }

    public long getPausingSince() {
        return pausingSince;
    }

    public long getResumingSince() {
        return resumingSince;
    }

    public long getLastrespawn() {
        return lastrespawn;
    }

    public long getRespawninterval() {
        return respawninterval;
    }

    public FC1DetailsMessageEvent(Object source, int mode, long starttime, long gametimer, long timeWhenTheFlagWasActivated, long maxgametime, long capturetime, long pausingSince, long resumingSince, long lastrespawn, long respawninterval, long resumeinterval) {
        super(source, mode);

        this.starttime = starttime;
        this.gametimer = gametimer;
        this.timeWhenTheFlagWasActivated = timeWhenTheFlagWasActivated;
        this.maxgametime = maxgametime;
        this.capturetime = capturetime;
        this.pausingSince = pausingSince;
        this.resumingSince = resumingSince;
        this.resumeinterval = resumeinterval;
        this.lastrespawn = lastrespawn;
        this.respawninterval = respawninterval;
    }

    public String getNextRespawn() {
        return respawninterval > 0l ? Tools.formatLongTime(lastrespawn + respawninterval - gametimer) : "--";
    }

    public String getRemaining() {
        long remaining = Farcry1AssaultThread.getEstimatedEndOfGame(super.gameState, maxgametime, timeWhenTheFlagWasActivated, capturetime);
        return Tools.formatLongTime(remaining - gametimer);
    }

    @Override
    public String toString() {
//        return "FC1DetailsMessageEvent{" +
//                "starttime=" + starttime +
//                ", gametimer=" + gametimer +
//                ", timeWhenTheFlagWasActivated=" + timeWhenTheFlagWasActivated +
//                ", maxgametime=" + maxgametime +
//                ", capturetime=" + capturetime +
//                ", pausingSince=" + pausingSince +
//                ", resumingSince=" + resumingSince +
//                "} " + super.toString();

        String result = "<style type=\"text/css\">\n" +
                ".tg  {border-collapse:collapse;border-spacing:0;border-color:#aabcfe;}\n" +
                ".tg td{font-family:Arial, sans-serif;font-size:14px;padding:10px 5px;border-style:solid;border-width:1px;overflow:hidden;word-break:normal;border-color:#aabcfe;color:#669;background-color:#e8edff;}\n" +
                ".tg th{font-family:Arial, sans-serif;font-size:14px;font-weight:normal;padding:10px 5px;border-style:solid;border-width:1px;overflow:hidden;word-break:normal;border-color:#aabcfe;color:#039;background-color:#b9c9fe;}\n" +
                ".tg .tg-jbmi{font-size:100%%;vertical-align:top}\n" +
                ".tg .tg-da58{background-color:#D2E4FC;font-size:100%%;text-align:center;vertical-align:top}\n" +
                "</style>\n" +
                "<table class=\"tg\">\n" +
                "  <tr>\n" +
                "    <th class=\"tg-jbmi\">%s</th>\n" +
                "    <th class=\"tg-jbmi\">%s</th>\n" +
                "    <th class=\"tg-jbmi\">%s</th>\n" +
                "    <th class=\"tg-jbmi\">%s</th>\n" +
                "    <th class=\"tg-jbmi\">%s</th>\n" +
                "    <th class=\"tg-jbmi\">%s</th>\n" +
                "    <th class=\"tg-jbmi\">%s</th>\n" +
                "    <th class=\"tg-jbmi\">%s</th>\n" +
                "  </tr>\n" +
                "  <tr>\n" +
                "    <td class=\"tg-da58\">%s</td>\n" +
                "    <td class=\"tg-da58\">%s</td>\n" +
                "    <td class=\"tg-da58\">%s</td>\n" +
                "    <td class=\"tg-da58\">%s</td>\n" +
                "    <td class=\"tg-da58\">%s</td>\n" +
                "    <td class=\"tg-da58\">%s</td>\n" +
                "    <td class=\"tg-da58\">%s</td>\n" +
                "    <td class=\"tg-da58\">%s</td>\n" +
                "  </tr>\n" +
                "</table>";

        return String.format(result,
                "respawn", "gametmr", "remain", "flagact", "maxgmtmr", "capttmr", "pause", "resume",
                getNextRespawn(), Tools.formatLongTime(gametimer), getRemaining(), Tools.formatLongTime(timeWhenTheFlagWasActivated),
                Tools.formatLongTime(maxgametime), Tools.formatLongTime(capturetime), Tools.formatLongTime(pausingSince == -1l ? pausingSince : System.currentTimeMillis() - pausingSince),
                Tools.formatLongTime(resumingSince == -1l ? resumingSince : System.currentTimeMillis() - resumingSince)
        );

    }
}
