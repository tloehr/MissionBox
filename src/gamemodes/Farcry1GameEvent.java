package gamemodes;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EventListener;
import java.util.EventObject;

/**
 * Created by Torsten on 05.07.2016.
 */
public class Farcry1GameEvent {

    // diese Zeit wird als Echtzeit dargestellt.
    private long eventStartTime;

    // diese Zeiten sind relativ zum Spiel Startzeitpunkt.
    // sie werden also von 0 aus gerechnet und erst später in
    // Echtzeit umgerechnet.
    // Dadurch wird die Verzögerung während der Pausen eingerechnet.
    private long eventDuration; // wie lange hat dieses Ereignis gedauert, bevor es vom nächsten abgelöst wurde.

    // in welchem Zustand befindet sich das Spiel ?
    private int gameState;

    // wie stand der Gametimer zum Zeitpunkt des Events. gametimer fangen bei 0 an.
    private long gametimer;

    private Logger logger;

    // wie soll bei der Zeitabfrage gerechnet werden ? Zu Beginn des Events oder zum Ende.
    private boolean endOfEvent = true;

    private JButton btnStartOfEvent, btnEndOfEvent;
    private JLabel lbl;
    private GameEventListener gameEventListener;

    public Farcry1GameEvent(int gameState, long gametimer) {
        btnStartOfEvent = new JButton(new ImageIcon((Farcry1GameEvent.class.getResource("/artwork/22x22/3leftarrow.png"))));
        btnEndOfEvent = new JButton(new ImageIcon((Farcry1GameEvent.class.getResource("/artwork/22x22/3rightarrow.png"))));

        btnStartOfEvent.addActionListener(e -> {
            endOfEvent = false;
            gameEventListener.eventSent(this);
        });
        btnEndOfEvent.addActionListener(e -> {
            endOfEvent = true;
            gameEventListener.eventSent(this);
        });
        lbl = new JLabel(toString());

        this.gameState = gameState;
        this.eventStartTime = System.currentTimeMillis();
        this.eventDuration = -1l; // not yet
        this.gametimer = gametimer;
        refreshTextLine();
    }

    /**
     * Bevor das nächste Ereignis eintritt, muss dieses erst abgeschlossen werden.
     * Erst in diesem Moment kann entschieden werden, wie lange dieses Ereignis angehalten hat.
     */
    public void finalizeEvent() {
        long now = System.currentTimeMillis();
        this.eventDuration = now - eventStartTime - 1;
        refreshTextLine();
    }

    /**
     * wenn der Spielzustand auf dieses Ereignis zurückgesetzt wird, dann ist es auch wieder aktiv.
     * somit läuft dieses Ereignis jetzt weiter und es nicht mehr FINALIZED.
     */
    public void unfinalizeEvent() {
        this.eventDuration = -1;
        refreshTextLine();
    }

    public long getEventDuration() {
        return eventDuration;
    }

    private void refreshTextLine() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                lbl.setText(Farcry1GameEvent.this.toString());
            }
        });
    }

    public int getGameState() {
        return gameState;
    }

    public long getEventStartTime() {
        return eventStartTime;
    }

//    public long getGametimer() {
//        return eventDuration == -1l ? -1l : (endOfEvent ? gametimer + eventDuration : gametimer);
//    }

        public long getGametimer() {
            return gametimer;
        }

    public long getEndtime(long maxgametime, long capturetime) {
        long endtime = maxgametime;
        if (gameState == Farcry1AssaultThread.GAME_FLAG_HOT) {
            endtime = gametimer + capturetime;
        }
        return endtime;
    }

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
//    public DateTime getEndtime() {
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


    public JPanel getGUI() {
        JPanel pnl = new JPanel();
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.LINE_AXIS));

        pnl.add(lbl);
        pnl.add(btnStartOfEvent);
        pnl.add(btnEndOfEvent);

        return pnl;
    }

    public void setGameEventListener(GameEventListener al){
       gameEventListener = al;
    }

    @Override
    public String toString() {
        String html = "<b>" + new DateTime(eventStartTime).toString(DateTimeFormat.mediumTime()) + "</b>&nbsp;";
        html += eventDuration == -1 ? "--&nbsp;" : eventDuration + "ms&nbsp;";
        html += Farcry1AssaultThread.GAME_MODES[gameState];

        return "<html>" + html + "</html>";

//        return "Farcry1GameEvent{" +
//                "gameState=" + Farcry1AssaultThread.GAME_MODES[gameState] +
//                ", flagactivation=" + flagactivation +
//                ", eventStartTime=" + eventStartTime +
//                '}';
    }
}
