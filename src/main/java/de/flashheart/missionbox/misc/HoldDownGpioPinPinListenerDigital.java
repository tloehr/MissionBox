package de.flashheart.missionbox.misc;

import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

public class HoldDownGpioPinPinListenerDigital implements GpioPinListenerDigital, HasLogger {
    volatile private boolean isRunning = false;
       volatile private boolean mouseDown = false;
       volatile private boolean reactedupon = false;
       volatile private long holding = 0l;

       private final long reactiontime;

    public HoldDownGpioPinPinListenerDigital(long reactiontime) {
        this.reactiontime = reactiontime;
    }

    @Override
    public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {

    }
}
