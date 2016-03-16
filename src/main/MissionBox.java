package main;

import com.pi4j.io.gpio.GpioController;
import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import gamemodes.Farcry1Assault;
import org.apache.log4j.*;

import java.io.File;


/**
 * Created by tloehr on 22.04.15.
 */
public class MissionBox {

    private static final Logger logger = Logger.getRootLogger();
    public static Level logLevel = Level.DEBUG;
    private static GpioController GPIO;
    private static FrmTest frmTest;

    public static final void main(String[] args) throws Exception {
        logLevel = Level.toLevel("DEBUG", Level.DEBUG);

//        try {
//            GPIO = GpioFactory.getInstance();
//        } catch (Exception e) {
//            GPIO = null;
//        }

        GPIO = null;


        String text = "10 minutes";


        Voice voice;
        VoiceManager voiceManager = VoiceManager.getInstance();
        voice = voiceManager.getVoice("kevin"); // kevin, kevin16, alan
        voice.allocate();
        voice.speak(text);


        PatternLayout layout = new PatternLayout("%d{ISO8601} %-5p [%t] %c: %m%n");
        logger.addAppender(new ConsoleAppender(layout));
        logger.addAppender(new FileAppender(layout, System.getenv("user.home") + File.separator + "missionbox.log"));


//            FrmMain frmMain = new FrmMain(new MessageListener() {
//                @Override
//                public void messageReceived(MessageEvent messageEvent) {
//                    logger.debug(messageEvent.getMessage());
//                }
//            });
//            frmMain.pack();
//            frmMain.setVisible(true);

        frmTest = new FrmTest();
        frmTest.pack();
        frmTest.setVisible(true);

        Farcry1Assault fc = new Farcry1Assault(GPIO, frmTest);
    }


}
