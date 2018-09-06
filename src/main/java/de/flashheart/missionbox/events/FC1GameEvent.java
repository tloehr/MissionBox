package de.flashheart.missionbox.events;


import de.flashheart.missionbox.misc.Tools;
import de.flashheart.missionbox.rlggames.GameEvent;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import java.util.EventObject;

/**
 * Dieses Event beinhaltet jedes Detail eines Farcry1 Zeit Ereignisses
 */
public class FC1GameEvent extends EventObject {

    private long starttime = -1l;

    private long timeWhenTheFlagWasActivated = -1l; // wann wurde die Flagge zuletzt aktiviert. -1l heisst, nicht aktiv.
    private long maxgametime = 0l; // wie lange kann dieses Spiel maximal laufen
    private long capturetime = 0l; // wie lange muss die Flagge gehalten werden bis sie erobert wurde ?

    private long pausingSince = -1l;
    private long resumingSince = -1l;
    private long lastrespawn;
    private long respawninterval;
    private long resumeinterval;


    private DateTime pit;
    private String event;

    private long gametime;
    private long remaining;

    public static final String css = "<style type=\"text/css\">\n" +
            ".tg  {border-collapse:collapse;border-spacing:0;border-color:#aabcfe;}\n" +
            ".tg td{font-family:Arial, sans-serif;font-size:14px;padding:10px 5px;border-style:solid;border-width:1px;overflow:hidden;word-break:normal;border-color:#aabcfe;color:#669;background-color:#e8edff;}\n" +
            ".tg th{font-family:Arial, sans-serif;font-size:14px;font-weight:normal;padding:10px 5px;border-style:solid;border-width:1px;overflow:hidden;word-break:normal;border-color:#aabcfe;color:#039;background-color:#b9c9fe;}\n" +
            ".tg .tg-jbmi{font-size:100%%;vertical-align:top}\n" +
            ".tg .tg-da58{background-color:#D2E4FC;font-size:100%%;text-align:center;vertical-align:top}\n" +
            "</style>\n";

    public long getResumeinterval() {
        return resumeinterval;
    }

    public long getStarttime() {
        return starttime;
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

//    private long getRemaining(long eventDuration) {
//        long endtime = maxgametime;
//        if (gameState == Farcry1AssaultThread.GAME_FLAG_HOT) {
//            endtime = timeWhenTheFlagWasActivated + capturetime;
//        }
//        return endtime - gametimer - Math.max(eventDuration, 0);
//    }

    public FC1GameEvent(Object source, String event, long starttime, long gametime, long timeWhenTheFlagWasActivated, long maxgametime, long capturetime, long pausingSince, long resumingSince, long lastrespawn, long respawninterval, long resumeinterval, long remaining) {


        super(source);
        this.pit = new DateTime();
        this.event = event;
        this.gametime = gametime;
        this.remaining = remaining;


        this.starttime = starttime;

        this.timeWhenTheFlagWasActivated = timeWhenTheFlagWasActivated;
        this.maxgametime = maxgametime;
        this.capturetime = capturetime;
        this.pausingSince = pausingSince;
        this.resumingSince = resumingSince;
        this.resumeinterval = resumeinterval;
        this.lastrespawn = lastrespawn;
        this.respawninterval = respawninterval;


    }


    public long getOvertime() {
        return gametime - maxgametime;
    }

    public boolean isOvertime() {
//        logger.debug("overtime gametimer:" + gametimer + " maxgametime:" + maxgametime);
        return gametime > maxgametime;
    }

//    public long getNextRespawn() {
//        return lastrespawn + respawninterval - gametimer;
//    }


    @Override
    public String toString() {
        return toString(-1l);
    }

    public String toString(long finalizedEventDuration) {
        String result = "\n" + StringUtils.repeat("-", 90) + "\n" +
                "|%9s|%9s|%9s|%9s|%9s|%9s|%9s|%9s|%9s|\n" +
                StringUtils.repeat("-", 90) + "\n" +
                "|%9s|%9s|%9s|%9s|%9s|%9s|%9s|%9s|%9s|\n" +
                StringUtils.repeat("-", 90) + "\n\n";
        return String.format(result,
                "gmstate", "gametmr", "remain", "flagact", "lrespawn", "maxgmtmr", "capttmr", "pause", "resume",
                event,
                Tools.formatLongTime(gametime + Math.max(finalizedEventDuration, 0)),
                Tools.formatLongTime(remaining),
                Tools.formatLongTime(timeWhenTheFlagWasActivated),
                Tools.formatLongTime(lastrespawn), Tools.formatLongTime(maxgametime), Tools.formatLongTime(capturetime), Tools.formatLongTime(pausingSince == -1l ? pausingSince : System.currentTimeMillis() - pausingSince),
                Tools.formatLongTime(resumingSince == -1l ? resumingSince : resumingSince + resumeinterval - System.currentTimeMillis())


        );
    }


    public String toHTML(String css, long finalizedEventDuration) {

//        logger.debug(toString());

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
                "gametmr", "remain", "flagact", "lrespawn", "maxgmtmr", "capttmr", "pause", "resume",
                Tools.formatLongTime(gametime + Math.max(finalizedEventDuration, 0), "HH:mm:ss"),
                Tools.formatLongTime(remaining, "HH:mm:ss"),
                Tools.formatLongTime(timeWhenTheFlagWasActivated, "mm:ss"),
                Tools.formatLongTime(lastrespawn, "HH:mm:ss"),
                Tools.formatLongTime(maxgametime, "HH:mm:ss"),
                Tools.formatLongTime(capturetime, "HH:mm:ss"),
                Tools.formatLongTime(pausingSince == -1l ? pausingSince : System.currentTimeMillis() - pausingSince, "HH:mm:ss"),
                Tools.formatLongTime(resumingSince == -1l ? resumingSince : resumingSince + resumeinterval - System.currentTimeMillis(), "HH:mm:ss")
        );


    }

    public long getGametime() {
        return gametime;
    }

    public long getRemaining() {
        return remaining;
    }

    public String getEvent() {
        return event;
    }

    public String toHTML() {
        return toHTML(css, -1l);
    }


    public String toPHPArray() {
        return "   ['pit' => '" + pit.toString("HH:mm:ss") + "','event' => '" + event + "','remaining' => '" + Tools.formatLongTime(remaining, "HH:mm:ss") + "'],\n";
    }

    public GameEvent createGameEvent() {
        return new GameEvent(pit.getMillis(), event, gametime, remaining);
    }
}
