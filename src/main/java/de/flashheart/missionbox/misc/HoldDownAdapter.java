package de.flashheart.missionbox.misc;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * https://stackoverflow.com/questions/6828684/java-mouseevent-check-if-pressed-down
 */
public class HoldDownAdapter extends MouseAdapter implements HasLogger {
    volatile private boolean isRunning = false;
    volatile private boolean mouseDown = false;
    volatile private boolean reactedupon = false;
    volatile private long holding = 0l;

    private final long reactiontime;

    public HoldDownAdapter(long reactiontime, ActionEvent actionEvent) {
        super();
        this.reactiontime = reactiontime;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            mouseDown = true;
            holding = System.currentTimeMillis();
            initThread();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            holding = 0l;
            reactedupon = false;
            mouseDown = false;
        }
    }

    private synchronized boolean checkAndMark() {
        if (isRunning) return false;
        isRunning = true;
        return true;
    }

    private void initThread() {
        if (checkAndMark()) {
            new Thread(() -> {
                do {
                    long heldfor = System.currentTimeMillis() - holding;
                    if (!reactedupon && heldfor > reactiontime){
                        reactedupon = true;
                        getLogger().debug("reacting once");
                    }
                    getLogger().debug("holding down for: "+heldfor / 1000);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } while (mouseDown);
                isRunning = false;
            }).start();
        }
    }

}
