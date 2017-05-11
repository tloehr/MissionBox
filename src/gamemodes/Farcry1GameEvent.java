package gamemodes;

import interfaces.FC1DetailsMessageEvent;
import main.MissionBox;
import misc.Tools;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Torsten on 05.07.2016.
 */
public class Farcry1GameEvent extends JPanel {
    private long pit;

    // wie lange hat dieser Event gedauert.
    private long eventDuration = -1;

    private Logger logger = Logger.getLogger(getClass());

    private JButton btnRevert;
    private JLabel lbl;
    private GameEventListener gameEventListener;

    private final FC1DetailsMessageEvent messageEvent;

    public long getPit() {
        return pit;
    }

    public Farcry1GameEvent(FC1DetailsMessageEvent messageEvent, Icon icon) {
        super();
        this.messageEvent = messageEvent;
        pit = System.currentTimeMillis();
        logger.setLevel(MissionBox.getLogLevel());
        logger.debug(pit + ">> new event");
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        btnRevert = new JButton(new ImageIcon((Farcry1GameEvent.class.getResource("/artwork/agt_reload32.png"))));
        btnRevert.setEnabled(false);

        btnRevert.addActionListener(e -> {
            gameEventListener.eventSent(this);
        });
        lbl = new JLabel(toString(), icon, SwingConstants.LEADING);
        lbl.setFont(new Font("Dialog", Font.BOLD, 16));

        add(btnRevert);
        btnRevert.setVisible(false);
        add(lbl);

        refreshTextLine();
    }

    public void setEnabled(boolean enabled) {
        btnRevert.setEnabled(enabled);
    }

    /**
     * Bevor das nächste Ereignis eintritt, muss dieses erst abgeschlossen werden.
     * Erst in diesem Moment kann entschieden werden, wie lange dieses Ereignis angehalten hat.
     */
    public void finalizeEvent(long gametimer) {
        eventDuration = gametimer - messageEvent.getGametimer(); // also der aktuelle gametimer minus dem gametimer zum Start dieses Events.
        
//        if (messageEvent.getGameState() == Farcry1AssaultThread.GAME_FLAG_HOT){
//            messageEvent.setRemaining(messageEvent.getTimeWhenTheFlagWasActivated() + messageEvent.getCapturetime() - gametimer);
//        } else if (messageEvent.getGameState() == Farcry1AssaultThread.GAME_FLAG_COLD){
//            messageEvent.setRemaining(messageEvent.getMaxgametime() - gametimer);
//        }

        logger.debug("\n  ___ _           _ _          ___             _   \n" +
                " | __(_)_ _  __ _| (_)______  | __|_ _____ _ _| |_ \n" +
                " | _|| | ' \\/ _` | | |_ / -_) | _|\\ V / -_) ' \\  _|\n" +
                " |_| |_|_||_\\__,_|_|_/__\\___| |___|\\_/\\___|_||_\\__|\n" +
                "                                                   ");
        logger.debug(pit + ">> finalizing gametime: " + (gametimer - 1));
        logger.debug(pit + ">> finalizing eventduration: " + eventDuration);
        logger.debug(pit + ">> wenn du hier hin zurück kehrst landest du bei diesen Zeiten");
        logger.debug(messageEvent.toString(eventDuration));

        logger.debug("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n\n");

        // ein Revert macht nur Sinn bei HOT oder COLD. Sonst nicht.
        btnRevert.setVisible(messageEvent.getGameState() == Farcry1AssaultThread.GAME_FLAG_HOT || messageEvent.getGameState() == Farcry1AssaultThread.GAME_FLAG_COLD);
        refreshTextLine();
    }

    public long getEventDuration() {
        return eventDuration;
    }

    public void refreshTextLine() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                lbl.setText(Farcry1GameEvent.this.toString());
            }
        });
    }

    public FC1DetailsMessageEvent getMessageEvent() {
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
        String html = "<b>" + new DateTime(pit).toString("HH:mm:ss") + "</b> ";// + (eventDuration == -1 ? "" : messageEvent.toHTML("", eventDuration));

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
