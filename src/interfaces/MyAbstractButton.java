package interfaces;

import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.event.GpioPinListener;

import javax.swing.*;
import java.awt.event.ActionListener;

/**
 * Created by tloehr on 15.03.16.
 */
public class MyAbstractButton {

    private final GpioPinDigitalInput gpio;
    private final JButton btn;

    public MyAbstractButton(GpioPinDigitalInput gpio, JButton btn) {
        this.gpio = gpio;
        this.btn = btn;
    }

    public void addListener(GpioPinListener var1) {
        if (gpio == null) return;
        gpio.addListener(var1);
    }

    public void addListener(ActionListener var1) {
        if (btn == null) return;
        btn.addActionListener(var1);
    }

    public boolean isLow(){
        return gpio != null ? gpio.isLow() : false;
    }

}
