package interfaces;


import gamemodes.Farcry1AssaultThread;
import misc.Tools;
import org.apache.commons.lang3.StringUtils;

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

    public long getNextRespawn() {
        return lastrespawn + respawninterval - gametimer;
    }

    public long getRemaining() {
        long remaining = Farcry1AssaultThread.getEstimatedEndOfGame(super.gameState, maxgametime, timeWhenTheFlagWasActivated, capturetime);
        return remaining - gametimer;
    }

    @Override
    public String toString() {
        String result = "\n" + StringUtils.repeat("-", 81) + "\n" +
                "|%8s|%8s|%8s|%8s|%8s|%8s|%8s|%8s|%8s|\n" +
                StringUtils.repeat("-", 81) + "\n" +
                "|%8s|%8s|%8s|%8s|%8s|%8s|%8s|%8s|%8s|\n" +
                StringUtils.repeat("-", 81) + "\n\n";
        return String.format(result,
                "gmstate", "gametmr", "remain", "flagact", "respawn", "maxgmtmr", "capttmr", "pause", "resume",
                Farcry1AssaultThread.GAME_STATES[gameState], Tools.formatLongTime(gametimer), Tools.formatLongTime(getRemaining()), Tools.formatLongTime(timeWhenTheFlagWasActivated),
                Tools.formatLongTime(getNextRespawn()), Tools.formatLongTime(maxgametime), Tools.formatLongTime(capturetime), Tools.formatLongTime(pausingSince == -1l ? pausingSince : System.currentTimeMillis() - pausingSince),
                Tools.formatLongTime(resumingSince == -1l ? resumingSince : resumingSince + resumeinterval - System.currentTimeMillis())

                // todo: der remain eines FLAGHOT ist immer 00:20 wenn der Event abgeschlossen wird. Warum ?
                // todo: sollte auch zwei angaben stehen. einmal die remaining zeit insgesamt (also wenn es COLD wäre). einmal bei FLAG die verkürzte zeit
        );
    }


    public String toHTML(String css) {

        logger.debug(toString());

        String result = css +
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
                "gametmr", "remain", "flagact", "respawn", "maxgmtmr", "capttmr", "pause", "resume",
                Tools.formatLongTime(gametimer, "mm:ss"), Tools.formatLongTime(getRemaining(), "mm:ss"), Tools.formatLongTime(timeWhenTheFlagWasActivated, "mm:ss"),
                Tools.formatLongTime(getNextRespawn(), "mm:ss"), Tools.formatLongTime(maxgametime, "mm:ss"), Tools.formatLongTime(capturetime, "mm:ss"), Tools.formatLongTime(pausingSince == -1l ? pausingSince : System.currentTimeMillis() - pausingSince, "mm:ss"),
                Tools.formatLongTime(resumingSince == -1l ? resumingSince : resumingSince + resumeinterval - System.currentTimeMillis(), "mm:ss")
        );


    }

    public String toHTML() {
        return toHTML("<style type=\"text/css\">\n" +
                ".tg  {border-collapse:collapse;border-spacing:0;border-color:#aabcfe;}\n" +
                ".tg td{font-family:Arial, sans-serif;font-size:14px;padding:10px 5px;border-style:solid;border-width:1px;overflow:hidden;word-break:normal;border-color:#aabcfe;color:#669;background-color:#e8edff;}\n" +
                ".tg th{font-family:Arial, sans-serif;font-size:14px;font-weight:normal;padding:10px 5px;border-style:solid;border-width:1px;overflow:hidden;word-break:normal;border-color:#aabcfe;color:#039;background-color:#b9c9fe;}\n" +
                ".tg .tg-jbmi{font-size:100%%;vertical-align:top}\n" +
                ".tg .tg-da58{background-color:#D2E4FC;font-size:100%%;text-align:center;vertical-align:top}\n" +
                "</style>\n");
    }
}
