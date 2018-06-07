package de.flashheart.missionbox.statistics;

import de.flashheart.missionbox.misc.HasLogger;

import javax.swing.*;
import java.util.Stack;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

public class MessageProcessor extends Thread implements HasLogger {

    private ReentrantLock lock;
    private boolean interrupted;
    private final Stack<PHPMessage> messageQ;
    private final CopyOnWriteArrayList<StatsSentListener> listeners;
    private FTPWrapper ftpWrapper;
    private boolean cleanupStatsFile = false;

    protected void fireChangeEvent(StatsSentEvent evt) {
        for (StatsSentListener l : listeners) {
            l.statsSentEventReceived(evt);
        }
    }

    public void addListener(StatsSentListener l) {
        this.listeners.add(l);
    }

    public boolean isInterrupted() {
        return interrupted;
    }

    public MessageProcessor() {
        super();

        this.listeners = new CopyOnWriteArrayList<>();

        lock = new ReentrantLock();
        messageQ = new Stack<>();
        interrupted = false;
    }

    public void pushMessage(PHPMessage message) {
        // https://github.com/tloehr/ocfflag/issues/4
        if (lock.isLocked()) return; // Sonst kann es passieren, dass das hier alles blockiert.

        lock.lock();
        try {
            getLogger().debug("pushMessage() pushing " + message.toString());
            messageQ.push(message);
        } finally {
            lock.unlock();
        }
    }

    public void cleanupStatsFile() {
        cleanupStatsFile = true;
    }

    public void testFTP(JTextArea outputArea, JButton buttonToDisable){
        if (lock.isLocked()) {
            outputArea.setText("MessageProcessor is busy. Try again.");
            return; // Sonst kann es passieren, dass das hier alles blockiert.
        }

        lock.lock();
        try {
            ftpWrapper.testFTP(outputArea, buttonToDisable);
        } finally {
            lock.unlock();
        }
    }

    public void run() {
        while (!interrupted) {
            try {
                lock.lock();
                // Um keine Verzögerungen beim Start zu haben, schiebe ich das hier in die Nebenläufigkeit.
                // Das wird nur einmal ausgeführt.
                if (ftpWrapper == null) ftpWrapper = new FTPWrapper();
                try {
                    if (!messageQ.isEmpty()) {
                        PHPMessage myMessage = messageQ.pop();

                        boolean move2archive = myMessage.getGameEvent().getEvent() == Statistics.EVENT_GAME_ABORTED ||
                                myMessage.getGameEvent().getEvent() == Statistics.EVENT_GAME_OVER ||
                                cleanupStatsFile;

                        boolean success = ftpWrapper.upload(myMessage.getPhp(), move2archive);
                        cleanupStatsFile = false; // muss nur einmal passieren
                        messageQ.clear(); // nur die letzte Nachricht ist wichtig

                        // sorge dafür, dass die weiße LED den erfolgreichen Versand anzeigt
                        fireChangeEvent(new StatsSentEvent(this, myMessage.getGameEvent(), success));
                    }

                    if (cleanupStatsFile) {
                        ftpWrapper.cleanupStatsFile();
                        cleanupStatsFile = false;
                    }

                } finally {
                    lock.unlock();
                }
                Thread.sleep(500); // Millisekunden
            } catch (InterruptedException ie) {
                interrupted = true;
            }
        }
    }
}
