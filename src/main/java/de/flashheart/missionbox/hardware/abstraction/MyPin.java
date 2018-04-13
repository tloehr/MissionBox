package de.flashheart.missionbox.hardware.abstraction;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.wiringpi.Gpio;
import de.flashheart.missionbox.misc.Tools;
import org.apache.log4j.Logger;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Synthesizer;
import javax.swing.*;
import java.awt.*;

/**
 * Created by tloehr on 07.06.15.
 */
public class MyPin {
    private final Logger logger = Logger.getLogger(getClass());
    private final GpioPinDigitalOutput outputPin;
    private final String name;
    private MyLED guiControlLED; // Diese MyLED wird zwecks debugging mit geschaltet.
    private int note = -1;
    private Synthesizer synthesizer = null;
    private MidiChannel[] channels;
    private Gpio gpio;
//
//
//    public MyPin(GpioController gpio, MCP23017GpioProvider gpioprovider, Pin pin, MyLED guiControlLED, String name) {
//        this(gpio, gpioprovider, pin, guiControlLED, name, -1, -1);
//    }
//
//    public MyPin(GpioController gpio, MCP23017GpioProvider gpioprovider, Pin pin, MyLED guiControlLED, String name, int instrument, int note) {
//        this(gpio == null ? null : gpio.provisionDigitalOutputPin(gpioprovider, pin, PinState.LOW), guiControlLED, name, instrument, note);
//    }


    private MyPin(GpioPinDigitalOutput outputPin, MyLED guiControlLED, String name, int instrument, int note) {
        this.name = name;
        this.guiControlLED = guiControlLED;
        this.outputPin = outputPin;
        if (outputPin != null) outputPin.setState(PinState.LOW);
        this.note = note;

        // Die Tonerzeugung ist nur zum Testen auf normalen Rechnern
        // und nicht auf dem RASPI. Da muss keine Rechenzeit verschwendet werden.
        if (!Tools.isArm()) {
            try {
                if (instrument > 0) {
                    synthesizer = MidiSystem.getSynthesizer();
                    synthesizer.open();
                    channels = synthesizer.getChannels();
                    channels[0].programChange(instrument);
                    synthesizer.loadAllInstruments(synthesizer.getDefaultSoundbank());
                }
            } catch (javax.sound.midi.MidiUnavailableException e) {
                synthesizer = null;
            }
        }
    }

    public MyPin(GpioController gpio, Pin pin, Color color, JPanel addYourself2this, String name) {
        this(gpio, pin, color, addYourself2this, name, -1, -1);
    }

    public MyPin(GpioController gpio, Pin pin, Color color, JPanel addYourself2this, String name, int instrument, int note) {
        this(gpio, pin, new MyLED(name, color), name, instrument, note);
        addYourself2this.add(guiControlLED);
    }

    public MyPin(GpioController gpio, Pin pin, MyLED guiControlLED, String name) {
        this(gpio, pin, guiControlLED, name, -1, -1);
    }

    public MyPin(GpioController gpio, Pin pin, MyLED guiControlLED, String name, int instrument, int note) {
        this(gpio == null ? null : gpio.provisionDigitalOutputPin(pin, PinState.LOW), guiControlLED, name, instrument, note);
    }

    public String getName() {
        return name;
    }

    public void setText(String text) {
        if (guiControlLED != null) guiControlLED.setText(text);
    }

    public String getText() {
        return guiControlLED != null ? guiControlLED.getToolTipText() : "";
    }

    public void setToolTipText(String text) {
        if (guiControlLED != null) guiControlLED.setToolTipText(text);
    }

    public void setState(boolean on) {
        if (outputPin != null) outputPin.setState(on ? PinState.HIGH : PinState.LOW);
        if (guiControlLED != null) guiControlLED.setState(on);

        if (synthesizer != null) {
            if (on) channels[0].noteOn(note, 90);
            else channels[0].noteOff(note);
        }
    }
}
