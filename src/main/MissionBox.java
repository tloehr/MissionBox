package main;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import gamemodes.Farcry1Assault;
import interfaces.MessageEvent;
import interfaces.MessageListener;
import org.apache.log4j.*;

import java.awt.*;
import java.io.File;


/**
 * Created by tloehr on 22.04.15.
 */
public class MissionBox {

    private static final Logger logger = Logger.getRootLogger();
    public static Level logLevel = Level.DEBUG;
    private static GpioController GPIO;

    public static final void main(String[] args) throws Exception {
        logLevel = Level.toLevel("DEBUG", Level.DEBUG);

        try {
            GPIO = GpioFactory.getInstance();
        } catch (Exception e){
            GPIO = null;
        }

        PatternLayout layout = new PatternLayout("%d{ISO8601} %-5p [%t] %c: %m%n");
        logger.addAppender(new ConsoleAppender(layout));
        logger.addAppender(new FileAppender(layout, System.getenv("user.home") + File.separator + "missionbox.log"));

        if (Desktop.isDesktopSupported()) {

            FrmMain frmMain = new FrmMain(new MessageListener() {
                @Override
                public void messageReceived(MessageEvent messageEvent) {
                    logger.debug(messageEvent.getMessage());
                }
            });
            frmMain.pack();
            frmMain.setVisible(true);
        }
        Farcry1Assault fc = new Farcry1Assault(GPIO);
    }
}
