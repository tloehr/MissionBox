package gamemodes;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import javax.swing.*;

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
    // wie lange wird das Spiel insgesamt dauern.
    private final long maxgametime;
    // wie lange ist die Capture Zeit
    private final long capturetime;
    // in welchem Zustand befindet sich das Spiel ?
    private int gameState;

    private long endtime;
    // wann hat dieses Ereignis stattgefunden ?

    private long gametimer;
    private Logger logger;

    private boolean endOfEvent = true;
    private JButton btnStartOfEvent, btnEndOfEvent;
    private JLabel lbl;


    public Farcry1GameEvent(int gameState, long gametimer, long maxgametime, long capturetime) {
        btnStartOfEvent = new JButton(new ImageIcon((Farcry1GameEvent.class.getResource("/artwork/22x22/3leftarrow.png"))));
        btnEndOfEvent = new JButton(new ImageIcon((Farcry1GameEvent.class.getResource("/artwork/22x22/3rightarrow.png"))));

        btnStartOfEvent.addActionListener(e -> endOfEvent = false);
        btnEndOfEvent.addActionListener(e -> endOfEvent = true);
        lbl = new JLabel(toString());

        this.maxgametime = maxgametime;
        this.capturetime = capturetime;
        this.gameState = gameState;
        this.eventStartTime = System.currentTimeMillis();
        this.eventDuration = -1l; // never
        this.gametimer = gametimer;
        refreshTextLine();

    }

    public long getEventDuration() {
        return eventDuration;
    }

    public void resetEventDuration() {
        this.eventDuration = -1l;
        refreshTextLine();
    }

    public void setEventDuration() {
        long now = System.currentTimeMillis();
        this.eventDuration = now - eventStartTime;
        refreshTextLine();
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

    public long getGametimer() {
        return gametimer;
    }

    public long getEndtime() {
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
