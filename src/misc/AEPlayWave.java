package misc;

/**
 * Created by tloehr on 26.04.15.
 */

import java.io.File;
import java.io.IOException;
import javax.sound.sampled.*;

public class AEPlayWave extends Thread {

    private String filename;
    private final LineListener lineListener;
    private SourceDataLine auline = null;

    private Position curPosition;

    private final int EXTERNAL_BUFFER_SIZE = 524288; // 128Kb

    enum Position {
        LEFT, RIGHT, NORMAL
    }


    public AEPlayWave(String wavfile, LineListener lineListener) {
        filename = wavfile;
        this.lineListener = lineListener;
        curPosition = Position.NORMAL;
    }

    public void stopSound(){
        if (auline != null && auline.isActive()){
            auline.stop();
        }
    }

    public AEPlayWave(String wavfile) {
        filename = wavfile;
        this.lineListener = null;
        curPosition = Position.NORMAL;
    }

//    public AEPlayWave(String wavfile, Position p) {
//        filename = wavfile;
//        curPosition = p;
//    }

    public void run() {

        File soundFile = new File(filename);
        if (!soundFile.exists()) {
            System.err.println("Wave file not found: " + filename);
            return;
        }

        AudioInputStream audioInputStream = null;
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
            e.printStackTrace();
            return;
        } catch (Exception e) {
            e.printStackTrace();
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

        auline.start();

        int nBytesRead = 0;
        byte[] abData = new byte[EXTERNAL_BUFFER_SIZE];

        try {
            while (nBytesRead != -1) {
                nBytesRead = audioInputStream.read(abData, 0, abData.length);
                if (nBytesRead >= 0) auline.write(abData, 0, nBytesRead);
//                if (isInterrupted()) {
//                    auline.stop();
//                    break;
//                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        } finally {
            auline.drain();
            auline.close();
        }

    }
}