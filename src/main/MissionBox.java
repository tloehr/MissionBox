package main;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
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

    public static final void main(String[] args) throws Exception {



//        System.out.println("asdjkasdj jdkasj als");
        logLevel = Level.toLevel("DEBUG", Level.DEBUG);
        GPIO = GpioFactory.getInstance();

        PatternLayout layout = new PatternLayout("%d{ISO8601} %-5p [%t] %c: %m%n");
        logger.addAppender(new ConsoleAppender(layout));
        logger.addAppender(new FileAppender(layout, System.getenv("user.home") + File.separator + "missionbox.log"));

        Farcry1Assault fc = new Farcry1Assault(GPIO);




    }


}
