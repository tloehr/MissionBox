package main;

import gamemodes.Farcry1Assault;
import misc.Config;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import java.awt.*;


/**
 * Created by tloehr on 22.04.15.
 */
public class MissionBox {
    private static final Logger logger = Logger.getRootLogger();
    private static PatternLayout defaultPatternLayout = new PatternLayout("%d{ISO8601} %-5p [%t] %c: %m%n");
    private static Level defaultLogLevel = Level.DEBUG;

    private static Config config;

    public static Config getConfig() {
        return config;
    }


    public static final void main(String[] args) throws Exception {

        // Default Logger setup until Config() is initialized
        ConsoleAppender consoleAppender = new ConsoleAppender(defaultPatternLayout);
        logger.addAppender(consoleAppender);
        logger.setLevel(defaultLogLevel);

        config = new Config();

        // no set the logger up properly according to the configs
        logger.removeAppender(consoleAppender);
        consoleAppender = new ConsoleAppender(config.getPatternLayout());
        logger.addAppender(consoleAppender);
        logger.setLevel(config.getLogLevel());




        FrmMain frmMain = new FrmMain();

        frmMain.setVisible(true);
        frmMain.setExtendedState(Frame.MAXIMIZED_BOTH);

    }


}
