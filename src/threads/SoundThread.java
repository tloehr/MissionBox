package threads;

import org.apache.log4j.Logger;

import javax.sound.sampled.LineEvent;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.UUID;


/**
 * Created by tloehr on 25.04.15.
 */
public class SoundThread implements Runnable {
    private final ResourceBundle lang;
    public final Logger LOGGER = Logger.getLogger("threads.SoundThread");

    HashMap<UUID, SoundClip> clips;

    private Thread thread;

    public SoundThread() {
        super();
        clips = new HashMap<>();
        thread = new Thread(this);
        lang = ResourceBundle.getBundle("Messages");
        thread.start();
    }

    public UUID playClip(String filename, int repeat) {
        UUID id = UUID.randomUUID();
        clips.put(id, new SoundClip(id, filename, repeat));
        return id;
    }

    public UUID playClip(String filename) {
        return playClip(filename, 1);
    }

    public void stopClip(UUID id) {
        if (clips.containsKey(id) && clips.get(id).getAePlayWave().isAlive()) {
            clips.get(id).getAePlayWave().stopSound();
        }
    }


    @Override
    public void run() {
        while (!thread.isInterrupted()) {
            try {
                Thread.sleep(50); // Milliseconds
            } catch (InterruptedException ie) {

                LOGGER.debug("PrintProcessor interrupted!");
            }
        }
    }


    class SoundClip {
        AEPlayWave aePlayWave;
        String filename;
        int repeat, playedtimes = 0;

        public SoundClip(UUID id, String filename, int repeat) {
            this.filename = filename;
            this.repeat = repeat;



            aePlayWave = new AEPlayWave(filename, event -> {
                if (event.getType() == LineEvent.Type.STOP) {
//                cycle++;
//                progressTarget.setValue(cycle);
                } else if (event.getType() == LineEvent.Type.CLOSE) {
                    clips.remove(id);
                }
            });

            clips.get(id).getAePlayWave().run();

        }


        public AEPlayWave getAePlayWave() {
            return aePlayWave;
        }
    }
}
