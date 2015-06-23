package main;

import gamemodes.Farcry1Assault;
import misc.Config;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;


/**
 * Created by tloehr on 22.04.15.
 */
public class MissionBox {
    private static final Logger logger = Logger.getRootLogger();

    private static Config config;

    public static Config getConfig() {
        return config;
    }


    public static final void main(String[] args) throws Exception {

        config = new Config();

        ConsoleAppender consoleAppender = new ConsoleAppender(config.getPatternLayout());
        logger.addAppender(consoleAppender);
        logger.setLevel(config.getLogLevel());

//        Farcry1Assault fc = new Farcry1Assault(config);

    }


}
