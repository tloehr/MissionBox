package de.flashheart.missionbox.gamemodes;

import de.flashheart.missionbox.Main;
import de.flashheart.missionbox.events.GameEventListener;
import de.flashheart.missionbox.events.FC1GameEvent;

import de.flashheart.missionbox.statistics.GameEvent;
import de.flashheart.missionbox.statistics.Statistics;
import de.flashheart.missionbox.misc.Tools;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import javax.swing.*;
import java.awt.*;
import java.util.UUID;

/**
 * Das sind Ereignisse zu denen man zurückspringen kann.
 */
public class FC1SavePoint extends JPanel {
    private final UUID uuid;
    private long remaining;
    private final long pit = System.currentTimeMillis();

    // wie lange hat dieser Event gedauert.
    private long eventDuration = -1;

    private Logger logger = Logger.getLogger(getClass());

    private JButton btnRevert;
    private JLabel lbl;
    private GameEventListener gameEventListener;

    private final FC1GameEvent messageEvent;

    public UUID getUuid() {
        return uuid;
    }

    public FC1SavePoint(FC1GameEvent messageEvent, Icon icon) {
        super();
        this.messageEvent = messageEvent;
        this.remaining = -1;
        uuid = UUID.randomUUID();
        logger.setLevel(Main.getLogLevel());
        logger.debug("new event: " + uuid);
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        btnRevert = new JButton(new ImageIcon((FC1SavePoint.class.getResource("/artwork/agt_reload32.png"))));
        btnRevert.setEnabled(false);

        btnRevert.addActionListener(e -> {
            gameEventListener.eventSent(this);
        });
        lbl = new JLabel(null, icon, SwingConstants.LEADING);
        lbl.setFont(new Font("Dialog", Font.BOLD, 16));

        add(btnRevert);
        btnRevert.setVisible(false);
        add(lbl);

        setToolTipText("<html>" + toString() + "</html>");
        refreshTextLine();
    }

    public void setEnabled(boolean enabled) {
        btnRevert.setEnabled(enabled);
    }

    /**
     * Bevor das nächste Ereignis eintritt, muss dieses erst abgeschlossen werden.
     * Erst in diesem Moment kann entschieden werden, wie lange dieses Ereignis angehalten hat.
     */
    public void finalizeEvent(long gametimer, long remaining) {

        eventDuration = gametimer - messageEvent.getGametime(); // also der aktuelle gametimer minus dem gametimer zum Start dieses Events.
        this.remaining = remaining;
        setToolTipText("<html>" + messageEvent.toHTML(FC1GameEvent.css, eventDuration) + "</html>");

        logger.debug("\n  ___ _           _ _          ___             _   \n" +
                " | __(_)_ _  __ _| (_)______  | __|_ _____ _ _| |_ \n" +
                " | _|| | ' \\/ _` | | |_ / -_) | _|\\ V / -_) ' \\  _|\n" +
                " |_| |_|_||_\\__,_|_|_/__\\___| |___|\\_/\\___|_||_\\__|\n" +
                "                                                   ");
        logger.debug("remaining: " + Tools.formatLongTime(remaining));
        logger.debug(toString());

        // ein Revert macht nur Sinn bei HOT oder COLD. Sonst nicht.
        btnRevert.setVisible(messageEvent.getEvent() == GameEvent.FUSED || messageEvent.getEvent() == GameEvent.DEFUSED);
        refreshTextLine();
    }

    public long getEventDuration() {
        return eventDuration;
    }

    public long getGametimerAtStart() {
        return messageEvent.getGametime();
    }

    public long getGametimerAtEnd() {
        return messageEvent.getGametime() + Math.max(eventDuration, 0);
    }

    public void refreshTextLine() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                lbl.setText(FC1SavePoint.this.toHTML());
            }
        });
    }

    public FC1GameEvent getMessageEvent() {
        return messageEvent;
    }

    public Icon getIcon() {
        return lbl.getIcon();
    }

    public void setGameEventListener(GameEventListener al) {
        gameEventListener = al;
    }

    @Override
    public String toString() {
        String result = "\n" + StringUtils.repeat("-", 90 + 39) + "\n" +
                "|%9s|%9s|%9s|%9s|%9s|%9s|%9s|%9s|%9s|%37s|\n" +
                StringUtils.repeat("-", 90 + 39) + "\n" +
                "|%9s|%9s|%9s|%9s|%9s|%9s|%9s|%9s|%9s|%37s|\n" +
                StringUtils.repeat("-", 90 + 39) + "\n\n";
        return String.format(result,
                "gmstate", "gametmr", "remain", "flagact", "lrespawn", "maxgmtmr", "capttmr", "pause", "resume", "uuid",
                messageEvent.getEvent(),
                Tools.formatLongTime(getGametimerAtEnd()),
                Tools.formatLongTime(remaining),
                Tools.formatLongTime(messageEvent.getTimeWhenTheFlagWasActivated()),
                Tools.formatLongTime(messageEvent.getLastrespawn()),
                Tools.formatLongTime(messageEvent.getMaxgametime()),
                Tools.formatLongTime(messageEvent.getCapturetime()),
                Tools.formatLongTime(messageEvent.getPausingSince() == -1l ? messageEvent.getPausingSince() : System.currentTimeMillis() - messageEvent.getPausingSince()),
                Tools.formatLongTime(messageEvent.getResumingSince() == -1l ? messageEvent.getResumingSince() : messageEvent.getResumingSince() + messageEvent.getResumeinterval() - System.currentTimeMillis()),
                uuid
        );
    }

    public String toHTML() {
        String html = "<b>" + new DateTime(pit).toString("HH:mm:ss") + "</b> gt@start: " + Tools.formatLongTime(getGametimerAtStart(),"mm:ss") +
                (eventDuration == -1 ? "" : " gt@end: " + Tools.formatLongTime(getGametimerAtEnd(),"HH:mm:ss"));

//        // Restliche Spielzeit (rmn - remaining)
//        html += (eventDuration == -1 ? "" : "gmrmn:" + Tools.formatLongTime(messageEvent.getMaxgametime() - messageEvent.getGametimer() - eventDuration - 1) + " ");
//
//        // Wie weit war die Flag (flg - flagtime)
//        if (messageEvent.getGameState() == Farcry1AssaultThread.GAME_FLAG_HOT) {
//            long endtime = messageEvent.getGametimer() + messageEvent.getCapturetime();
//            html += " " + (eventDuration == -1 ? "" : "<br/>flgrmn:" + Tools.formatLongTime(endtime - messageEvent.getGametimer() - eventDuration - 1) + " ");
//        }
//
//        html += eventDuration == -1 ? "-- " : "evtdur:" + new DateTime(eventDuration, DateTimeZone.UTC).toString("mm:ss:SSS] ");
//        html += Farcry1AssaultThread.GAMSTATS[messageEvent.getGameState()];

        return "<html>" + html + "</html>";

//        return "Farcry1GameEvent{" +
//                "gameState=" + Farcry1AssaultThread.GAMSTATS[gameState] +
//                ", flagactivation=" + flagactivation +
//                ", eventStartTime=" + eventStartTime +
//                '}';
    }
}
