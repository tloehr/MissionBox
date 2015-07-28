package interfaces;

import com.pi4j.io.gpio.GpioPinDigitalInput;

import javax.swing.event.EventListenerList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

/**
 * Created by tloehr on 05.07.15.
 */
public abstract class GameModeConfigs {

    protected Properties parameters = new Properties();

    protected ArrayList<PercentageInterface> listProgressLeft = new ArrayList<>();;



    public abstract void setProperty(String key, String value);
    public abstract void setButton(String key, GameButton btn, String gui);

    public Properties getParameters() {
        return parameters;
    }


    public ArrayList<PercentageInterface> getListProgressLeft() {
        return listProgressLeft;
    }

}
