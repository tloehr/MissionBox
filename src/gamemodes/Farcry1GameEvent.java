package gamemodes;

import interfaces.FC1DetailsMessageEvent;
import misc.Tools;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Torsten on 05.07.2016.
 */
public class Farcry1GameEvent extends JPanel {
//    private final long maxgametime;
//    private final long capturetime;

    // diese Zeit wird als Echtzeit dargestellt.
//    private long eventStartTime;

    // diese Zeiten sind relativ zum Spiel Startzeitpunkt.
    // sie werden also von 0 aus gerechnet und erst später in
    // Echtzeit umgerechnet.
    // Dadurch wird die Verzögerung während der Pausen eingerechnet.

    private long pit;

    // in welchem Zustand befindet sich das Spiel ?
//    private int gameState;

    // wie stand der Gametimer zum Zeitpunkt des Events. gametimer fangen bei 0 an.
//    private long gametimerAtStart;

    // wie lange hat dieser Event gedauert.
    private long evenDuration = -1;

    private Logger logger = Logger.getLogger(getClass());

    private JButton btnRevert;
    private JLabel lbl;
    private GameEventListener gameEventListener;

    private final FC1DetailsMessageEvent messageEvent;


    public Farcry1GameEvent(FC1DetailsMessageEvent messageEvent, Icon icon) {
        super();
        this.messageEvent = messageEvent;
        pit = System.currentTimeMillis();
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

    public void setEnabled(boolean enabled){
        btnRevert.setEnabled(enabled);
    }

    /**
     * Bevor das nächste Ereignis eintritt, muss dieses erst abgeschlossen werden.
     * Erst in diesem Moment kann entschieden werden, wie lange dieses Ereignis angehalten hat.
     */
    public void finalizeEvent(long gametimer) {
        evenDuration = gametimer - messageEvent.getGametimer();
        btnRevert.setVisible(true);
        refreshTextLine();
    }

    public long getEvenDuration() {
        return evenDuration;
    }

    private void refreshTextLine() {
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

    //    public long getGametimer() {
//        return eventDuration == -1l ? -1l : (endOfEvent ? gametimer + eventDuration : gametimer);
//    }


//    public long getMaxGametime() {
//        long endtime = maxgametime;
//        if (gameState == Farcry1AssaultThread.GAME_FLAG_HOT) {
//            endtime = gametimerAtStart + capturetime;
//        }
//        return endtime;
//    }

    //    public DateTime getNewFlagactivation(DateTime) {
//        Duration difference = new Duration(eventStartTime, new DateTime());
//        logger.debug("old flagactivation time " + flagactivation.toString());
//        logger.debug("difference " + difference.toString());
//        logger.debug("new flagactivation time " + flagactivation.plus(difference).toString());
//
//        return flagactivation.plus(difference);
//
//    }
//
//
//    public DateTime getMaxGametime() {
//        Duration difference = new Duration(eventStartTime, new DateTime());
//        return endtime.plus(difference);
//    }
//
//    public DateTime getStarttime() {
//        Duration difference = new Duration(eventStartTime, new DateTime());
//        logger.debug("old start time " + starttime.toString());
//        logger.debug("difference " + difference.toString());
//        logger.debug("new start time " + starttime.plus(difference).toString());
//
//        return starttime.plus(difference);
//    }

    public Icon getIcon() {
        return lbl.getIcon();
    }

    public void setGameEventListener(GameEventListener al) {
        gameEventListener = al;
    }

    @Override
    public String toString() {
        String html = "<b>" + new DateTime(pit).toString("HH:mm:ss") + "</b> ";

        // Restliche Spielzeit (rmn - remaining)
        html += (evenDuration == -1 ? "" : "gmrmn:" + Tools.formatLongTime(messageEvent.getMaxgametime() - messageEvent.getGametimer() - evenDuration - 1) + " ");

        // Wie weit war die Flag (flg - flagtime)
        if (messageEvent.getGameState() == Farcry1AssaultThread.GAME_FLAG_HOT) {
            long endtime = messageEvent.getGametimer() + messageEvent.getCapturetime();
            html += " " + (evenDuration == -1 ? "" : "<br/>flgrmn:" + Tools.formatLongTime(endtime - messageEvent.getGametimer() - evenDuration - 1) + " ");
        }

        html += evenDuration == -1 ? "-- " : "evtdur:" + new DateTime(evenDuration, DateTimeZone.UTC).toString("mm:ss:SSS] ");
//        html += Farcry1AssaultThread.GAME_STATES[messageEvent.getGameState()];

        return "<html>" + html + "</html>";

//        return "Farcry1GameEvent{" +
//                "gameState=" + Farcry1AssaultThread.GAME_STATES[gameState] +
//                ", flagactivation=" + flagactivation +
//                ", eventStartTime=" + eventStartTime +
//                '}';
    }
}
