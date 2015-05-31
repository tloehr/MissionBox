package interfaces;

import sun.audio.AudioPlayer;
import sun.audio.AudioStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Created by tloehr on 26.04.15.
 */
public class SoundClip {
    AudioStream audioStream;

    public SoundClip(File file) throws Exception {
        InputStream in = new FileInputStream(file);
        audioStream = new AudioStream(in);
    }

    public void play() {
        AudioPlayer.player.start(audioStream);

    }

    public void stop() {
        AudioPlayer.player.stop(audioStream);

    }
}
