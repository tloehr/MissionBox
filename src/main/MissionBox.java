package main;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import gamemodes.Farcry1Assault;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;


/**
 * Created by tloehr on 22.04.15.
 */
public class MissionBox {


    private static final Logger logger = Logger.getRootLogger();
    public static Level logLevel = Level.DEBUG;
    private final static GpioController GPIO = GpioFactory.getInstance();

    public static final void main(String[] args) throws Exception {


        PatternLayout layout = new PatternLayout("%d{ISO8601} %-5p [%t] %c: %m%n");
        ConsoleAppender consoleAppender = new ConsoleAppender(layout);
        logger.addAppender(consoleAppender);


        Farcry1Assault fc = new Farcry1Assault(GPIO);


    }

}
