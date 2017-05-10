package gamemodes;

import interfaces.FC1DetailsMessageEvent;
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

    public void setEnabled(boolean enabled) {
        btnRevert.setEnabled(enabled);
    }

    /**
     * Bevor das nächste Ereignis eintritt, muss dieses erst abgeschlossen werden.
     * Erst in diesem Moment kann entschieden werden, wie lange dieses Ereignis angehalten hat.
     */
    public void finalizeEvent(long gametimer) {
        evenDuration = gametimer - messageEvent.getGametimer();
        logger.debug("finalizing gametime: " + (gametimer - 1));
        btnRevert.setVisible(true);
        refreshTextLine();
    }

    public long getEvenDuration() {
        return evenDuration;
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
        String html = "<b>" + new DateTime(pit).toString("HH:mm:ss") + "</b> " + (evenDuration == -1 ? "" : messageEvent.toHTML(""));

//        // Restliche Spielzeit (rmn - remaining)
//        html += (evenDuration == -1 ? "" : "gmrmn:" + Tools.formatLongTime(messageEvent.getMaxgametime() - messageEvent.getGametimer() - evenDuration - 1) + " ");
//
//        // Wie weit war die Flag (flg - flagtime)
//        if (messageEvent.getGameState() == Farcry1AssaultThread.GAME_FLAG_HOT) {
//            long endtime = messageEvent.getGametimer() + messageEvent.getCapturetime();
//            html += " " + (evenDuration == -1 ? "" : "<br/>flgrmn:" + Tools.formatLongTime(endtime - messageEvent.getGametimer() - evenDuration - 1) + " ");
//        }
//
//        html += evenDuration == -1 ? "-- " : "evtdur:" + new DateTime(evenDuration, DateTimeZone.UTC).toString("mm:ss:SSS] ");
//        html += Farcry1AssaultThread.GAMSTATS[messageEvent.getGameState()];

        return "<html>" + html + "</html>";

//        return "Farcry1GameEvent{" +
//                "gameState=" + Farcry1AssaultThread.GAMSTATS[gameState] +
//                ", flagactivation=" + flagactivation +
//                ", eventStartTime=" + eventStartTime +
//                '}';
    }
}
