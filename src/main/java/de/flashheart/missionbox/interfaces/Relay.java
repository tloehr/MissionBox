package de.flashheart.missionbox.interfaces;

import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import de.flashheart.missionbox.Main;
import de.flashheart.missionbox.hardware.abstraction.MyLED;

import org.apache.log4j.Logger;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Synthesizer;
import javax.swing.*;
import java.awt.*;

/**
 * Created by tloehr on 07.06.15.
 */
public class Relay implements OnOffInterface {
    private final Logger logger = Logger.getLogger(getClass());
    private final GpioPinDigitalOutput pin;
    private final String name;
    private MyLED debugLED; // for on screen debugging
    private int note = -1;
    private Synthesizer synthesizer;
    private MidiChannel[] channels;
    private String text;

    private Relay(GpioPinDigitalOutput pin, String name) {
        if (Main.getGPIO() != null && pin == null) {
            logger.fatal("WRONG CONFIG FOR " + name);
            System.exit(1);
        }

        this.pin = pin;
        this.name = name;
        if (pin != null) pin.setState(PinState.LOW);
    }

//    protected Relay(String configKey, Color color, JPanel addYourself2this, int instrument, int note) {
//        this(MissionBox.getOutputMap().get(MissionBox.getConfig(configKey)), configKey, color, addYourself2this, instrument, note);
//
//    }

    public Relay(GpioPinDigitalOutput pin, String name, Color color, JPanel addYourself2this) {
        this(pin, name, color, addYourself2this, -1, -1);
    }

    public Relay(GpioPinDigitalOutput pin, String name, Color color, JPanel addYourself2this, int instrument, int note) {
        this(pin, name);
        debugLED = new MyLED(name, color);
        this.note = note;
        addYourself2this.add(debugLED);
        synthesizer = null;

        // Die Tonerzeugung ist nur zum Testen auf normalen Rechnern
        // und nicht auf dem RASPI. Da muss keine Rechenzeit verschwendet werden.
        if (Main.getGPIO() == null) {
            try {
                if (instrument > 0) {
                    synthesizer = MidiSystem.getSynthesizer();
                    synthesizer.open();
                    //                Instrument instrs[] = synthesizer.getDefaultSoundbank().getInstruments();
                    channels = synthesizer.getChannels();
                    channels[0].programChange(instrument);
                    synthesizer.loadAllInstruments(synthesizer.getDefaultSoundbank());
                }
            } catch (javax.sound.midi.MidiUnavailableException e) {
                synthesizer = null;
            }
        }
    }

//    protected Relay(String configKey, Color color, JPanel addYourself2this) {
//        this(MissionBox.getOutputMap().get(MissionBox.getConfig(configKey)), configKey);
//
//        if (addYourself2this != null) {
//            debugLED = new MyLED(configKey, color);
//            addYourself2this.add(debugLED);
//        }
//    }

    public void setText(String text) {
        this.text = text;
        if (!Main.getFrmTest().getTbDebug().isSelected()) return;
        SwingUtilities.invokeLater(() -> {
            debugLED.setText(text.isEmpty() ? name : name + " [" + text + "]");
            debugLED.revalidate();
            debugLED.repaint();
        });
    }

    public String getText() {
        return text;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setOn(boolean on) {
        if (pin != null) pin.setState(on ? PinState.HIGH : PinState.LOW);
        if (debugLED != null && Main.getFrmTest().getTbDebug().isSelected()) debugLED.setState(on);

        if (synthesizer != null) {
            if (on) channels[0].noteOn(note, 90);
            else channels[0].noteOff(note);
        }
    }
}
