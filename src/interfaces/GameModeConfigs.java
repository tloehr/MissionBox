package interfaces;

import com.pi4j.io.gpio.GpioPinDigitalInput;

import java.util.HashMap;
import java.util.Properties;

/**
 * Created by tloehr on 05.07.15.
 */
public abstract class GameModeConfigs {

    Properties parameters = new Properties();


    public abstract void setProperty(String key, String value);
    public abstract void setButton(String key, GameButton btn, String gui);

    public Properties getParameters() {
        return parameters;
    }
}
