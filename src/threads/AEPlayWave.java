package threads;

/**
 * Created by tloehr on 26.04.15.
 */

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import javax.sound.sampled.*;

public class AEPlayWave implements Runnable {

    private String filename;
    private final LineListener lineListener;
    private SourceDataLine auline = null;
    private Logger logger = Logger.getLogger(getClass());
    private AudioInputStream audioInputStream = null;
    private Thread thread;

    private Position curPosition;

    private int repeat;

    private final int EXTERNAL_BUFFER_SIZE = 524288; // 128Kb

    enum Position {
        LEFT, RIGHT, NORMAL
    }


    public AEPlayWave(String wavfile) {
        this(wavfile, null);
    }

    //auline.close();
    public AEPlayWave(String wavfile, LineListener lineListener) {
        thread = new Thread(this);
        filename = wavfile;
        this.lineListener = lineListener;
        curPosition = Position.NORMAL;


        File soundFile = new File(filename);
        if (!soundFile.exists()) {
            logger.error("Wave file not found: " + filename);
            return;
        }

        try {
            audioInputStream = AudioSystem.getAudioInputStream(soundFile);
        } catch (UnsupportedAudioFileException e1) {
            e1.printStackTrace();
            return;
        } catch (IOException e1) {
            e1.printStackTrace();
            return;
        }

        AudioFormat format = audioInputStream.getFormat();

        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

        try {
            auline = (SourceDataLine) AudioSystem.getLine(info);
            auline.open(format);
        } catch (LineUnavailableException e) {
            logger.error(e);
            return;
        } catch (Exception e) {
            logger.error(e);
            return;
        }

        if (auline.isControlSupported(FloatControl.Type.PAN)) {
            FloatControl pan = (FloatControl) auline
                    .getControl(FloatControl.Type.PAN);
            if (curPosition == Position.RIGHT)
                pan.setValue(1.0f);
            else if (curPosition == Position.LEFT)
                pan.setValue(-1.0f);
        }

        if (lineListener != null) {
            auline.addLineListener(lineListener);
        }

    }

    public void stopSound() {
        repeat = 0;
    }

    public void playSound() {
        repeat = 1;
    }

    @Override
    public void run() {
        while (!thread.isInterrupted()) {

            if (repeat > 0) {

                try {
                    int nBytesRead = 0;
                    byte[] abData = new byte[EXTERNAL_BUFFER_SIZE];

                    auline.start();

                    try {
                        while (nBytesRead != -1) {
                            nBytesRead = audioInputStream.read(abData, 0, abData.length);
                            if (nBytesRead >= 0) auline.write(abData, 0, nBytesRead);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    } finally {
                        auline.drain();

                    }

                    Thread.sleep(50);
                } catch (InterruptedException ie) {
                    break;
                }
            }

        }
    }
}