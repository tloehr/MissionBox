package de.flashheart.missionbox.statistics;

import de.flashheart.gamestate.GameState;
import de.flashheart.missionbox.Main;
import de.flashheart.missionbox.misc.Configs;
import de.flashheart.missionbox.misc.HasLogger;
import org.codehaus.plexus.util.Base64;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;


import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Stack;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

public class MessageProcessor extends Thread implements HasLogger {

    private ReentrantLock lock;
    private boolean interrupted;
    private final Stack<GameState> messageQ;
    private final CopyOnWriteArrayList<StatsSentListener> listeners;
    private boolean active;
    private final HttpHeaders headers;
    private final RestTemplate restTemplate;


    protected void fireChangeEvent(StatsSentEvent evt) {
        if (!active) return;
        for (StatsSentListener l : listeners) {
            l.statsSentEventReceived(evt);
        }
    }

    public boolean isInterrupted() {
        return interrupted;
    }

    public MessageProcessor() {
        super();
        active = Long.parseLong(Main.getConfigs().get(Configs.MIN_STAT_SEND_TIME)) > 0;

        //
        // Authentication
        //
        headers = new HttpHeaders();

        byte[] encodedAuth = Base64.encodeBase64(Main.getConfigs().get(Configs.REST_AUTH).getBytes(Charset.forName("US-ASCII")));
        String authHeader = "Basic " + new String(encodedAuth);
        headers.set("Authorization", authHeader);

        //                        headers.add("Accept", MediaType.APPLICATION_XML_VALUE);
        //                        headers.setContentType(MediaType.APPLICATION_XML);
        headers.setAccept(Arrays.asList(new MediaType[]{MediaType.APPLICATION_JSON}));
        headers.setContentType(MediaType.APPLICATION_JSON);
        restTemplate = new RestTemplate();

        listeners = new CopyOnWriteArrayList<>();

        lock = new ReentrantLock();
        messageQ = new Stack<>();
        interrupted = false;
    }

    public void pushMessage(GameState gameState) {
        if (!active) return;
        // https://github.com/tloehr/ocfflag/issues/4
        if (lock.isLocked()) return; // Sonst kann es passieren, dass das hier alles blockiert.

        lock.lock();
        try {
            getLogger().debug("pushMessage() pushing " + gameState);
            messageQ.push(gameState);
        } finally {
            lock.unlock();
        }
    }


//    public void testFTP(JTextArea outputArea, JButton buttonToDisable) {
//        if (lock.isLocked()) {
//            outputArea.setText("MessageProcessor is busy. Try again.");
//            return; // Sonst kann es passieren, dass das hier alles blockiert.
//        }
//
//        lock.lock();
//        try {
////            ftpWrapper.testFTP(outputArea, buttonToDisable);
//        } finally {
//            lock.unlock();
//        }
//    }

    public void run() {
        while (active && !interrupted) {
            try {
                lock.lock();
                // Um keine Verzögerungen beim Start zu haben, schiebe ich das hier in die Nebenläufigkeit.
                // Das wird nur einmal ausgeführt.
//                if (ftpWrapper == null) ftpWrapper = new FTPWrapper();
                try {
                    if (!messageQ.isEmpty()) {
                        GameState gameState = messageQ.pop();

                        // Data attached to the request.
                        HttpEntity<GameState> requestBody = new HttpEntity<>(gameState, headers);

                        // Send request with POST method.
                        GameState result = restTemplate.postForObject(Main.getConfigs().get(Configs.REST_URL), requestBody, GameState.class);

                        if (result != null && result.getBombname() != null) {
                            getLogger().debug("GameState created: " + result.getTimestamp());
                        } else {
                            getLogger().error("Something error!");
                        }

                        fireChangeEvent(new StatsSentEvent(this, gameState, true));
                    }

                } catch (Exception ex) {
                    getLogger().error(ex);
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
