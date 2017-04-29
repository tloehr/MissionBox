package gamemodes;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Torsten on 05.07.2016.
 */
public class Farcry1GameEvent extends JPanel {
    private final long maxgametime;
    private final long capturetime;

    // diese Zeit wird als Echtzeit dargestellt.
//    private long eventStartTime;

    // diese Zeiten sind relativ zum Spiel Startzeitpunkt.
    // sie werden also von 0 aus gerechnet und erst später in
    // Echtzeit umgerechnet.
    // Dadurch wird die Verzögerung während der Pausen eingerechnet.

    private long pit;

    // in welchem Zustand befindet sich das Spiel ?
    private int gameState;

    // wie stand der Gametimer zum Zeitpunkt des Events. gametimer fangen bei 0 an.
    private long gametimerAtStart;

    //wann war dieser Event zu Ende. Geht erst nach FINALIZE()
    private long gametimerAtEnd = -1;

    private Logger logger = Logger.getLogger(getClass());

    private JButton btnRevert;
    private JLabel lbl;
    private GameEventListener gameEventListener;

    public Farcry1GameEvent(int gameState, long gametimer, long maxgametime, long capturetime, Icon icon) {
        super();
        this.maxgametime = maxgametime;
        this.capturetime = capturetime;
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

        this.gameState = gameState;
        this.gametimerAtStart = gametimer;
        this.gametimerAtEnd = -1;
        refreshTextLine();
    }


    /**
     * Bevor das nächste Ereignis eintritt, muss dieses erst abgeschlossen werden.
     * Erst in diesem Moment kann entschieden werden, wie lange dieses Ereignis angehalten hat.
     */
    public void finalizeEvent(long gametimer) {
        gametimerAtEnd = gametimer;
//        logger.debug("calculated: " + gametimerAtEnd);
        btnRevert.setVisible(true);
        refreshTextLine();
    }

//    /**
//     * wenn der Spielzustand auf dieses Ereignis zurückgesetzt wird, dann ist es auch wieder aktiv.
//     * somit läuft dieses Ereignis jetzt weiter und es nicht mehr FINALIZED.
//     */
//    public void unfinalizeEvent() {
//        this.gametimerAtEnd = -1;
//        btnRevert.setVisible(false);
//        refreshTextLine();
//    }

    public long getGametimerAtEnd() {
        return gametimerAtEnd;
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

//    public long getGametimer() {
//        return eventDuration == -1l ? -1l : (endOfEvent ? gametimer + eventDuration : gametimer);
//    }


    public long getGametimerAtStart() {
        return gametimerAtStart;
    }

    public long getMaxGametime() {
        long endtime = maxgametime;
        if (gameState == Farcry1AssaultThread.GAME_FLAG_HOT) {
            endtime = gametimerAtStart + capturetime;
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


        html += (gametimerAtEnd == -1 ? "" : "(" + new DateTime(maxgametime - gametimerAtEnd + 1, DateTimeZone.UTC).toString("mm:ss") + ") ");


        if (gameState == Farcry1AssaultThread.GAME_FLAG_HOT) {
            html += " " + (gametimerAtEnd == -1 ? "" : "{" + new DateTime(getMaxGametime() - gametimerAtEnd + 1, DateTimeZone.UTC).toString("mm:ss") + "} ");
        }

        html += gametimerAtEnd == -1 ? "-- " : "[" + new DateTime(gametimerAtEnd - gametimerAtStart, DateTimeZone.UTC).toString("mm:ss:SSS] ");
        html += Farcry1AssaultThread.GAME_MODES[gameState];

        return "<html>" + html + "</html>";

//        return "Farcry1GameEvent{" +
//                "gameState=" + Farcry1AssaultThread.GAME_MODES[gameState] +
//                ", flagactivation=" + flagactivation +
//                ", eventStartTime=" + eventStartTime +
//                '}';
    }
}
