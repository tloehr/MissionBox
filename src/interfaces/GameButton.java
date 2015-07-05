package interfaces;

import com.pi4j.io.gpio.GpioPinDigitalInput;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

/**
 * Created by tloehr on 05.07.15.
 */
public class GameButton {

    protected ArrayList<GameButtonListener> listeners = new ArrayList<>();

    JButton jbutton = null;
    GpioPinDigitalInput gpioButton = null;

    public GameButton(JButton button) {
        jbutton = button;

        jbutton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                for (GameButtonListener listener : listeners) {
                    listener.buttonUp(new GameButtonEvent(e));
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                for (GameButtonListener listener : listeners) {
                    listener.buttonDown(new GameButtonEvent(e));
                }
            }
        });
    }

    public GameButton(GpioPinDigitalInput button) {
        gpioButton = button;
    }

    protected void broadcast(GameButtonEvent gbe) {
        for (GameButtonListener listener : listeners) {
            listener.
        }
    }


}
