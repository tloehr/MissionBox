package de.flashheart.missionbox.threads;


import de.flashheart.missionbox.hardware.abstraction.MyPin;
import de.flashheart.missionbox.hardware.abstraction.MyRGBLed;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This handler runs parallel to the main programm and handles all the blinking needs of the specific pins. It knows to handle collision between pins that must not be run at the same time.
 * Dieser Handler läuft parallel zum Hauptprogramm. Er steuert alles Relais und achtet auch auf widersprüchliche Befehle und Kollisionen (falls bestimmte Relais nicht gleichzeitig anziehen dürfen, gibt mittlerweile nicht mehr).
 */

public class PinHandler {
    public static final String FOREVER = "∞";

    final Logger logger;
    final ReentrantLock lock;
    final HashMap<String, GenericBlinkModel> pinMap;
    final HashMap<String, Future<String>> futures;
    private ExecutorService executorService;
    final HashMap<String, String> schemes;

    public PinHandler() {
        lock = new ReentrantLock();
        pinMap = new HashMap<>();
        futures = new HashMap<>();
        logger = Logger.getLogger(getClass());
        schemes = new HashMap<>();

        executorService = Executors.newFixedThreadPool(20);

    }

    /**
     * adds a a relay to the handler.
     *
     * @param myPin der betreffende Pin
     */
    public void add(MyPin myPin) {
        lock.lock();
        try {
            pinMap.put(myPin.getName(), new PinBlinkModel(myPin));
        } finally {
            lock.unlock();
        }
    }

    /**
     * adds a a relay to the handler.
     *
     * @param myRGB der betreffende Pin
     */
    public void add(MyRGBLed myRGB) {
        lock.lock();
        try {
            pinMap.put(myRGB.getName(), new RGBBlinkModel(myRGB));
        } finally {
            lock.unlock();
        }
    }


    private void setScheme(String name, String text, String scheme) {
        logger.debug(name + "-" + scheme);
        lock.lock();
        try {
            GenericBlinkModel genericBlinkModel = pinMap.get(name);

            if (genericBlinkModel != null) {
                if (futures.containsKey(name) && !futures.get(name).isDone()) { // but only if it runs
                    //logger.debug("terminating: " + name);
                    futures.get(name).cancel(true);
                }
                schemes.put(name, scheme); // aufbewahren für die Wiederherstellung nach der Pause
                genericBlinkModel.setScheme(scheme);
                futures.put(name, executorService.submit(genericBlinkModel));
            } else {
                logger.error("Element not found in handler");
            }
        } catch (Exception e) {
            logger.trace(e);
            logger.fatal(e);
            System.exit(0);
        } finally {
            lock.unlock();
        }
    }

    public void setScheme(String name, String scheme) {
        setScheme(name, null, scheme);
    }

    public void off(String name) {
        setScheme(name, "0:");
    }

    public void off() {
        for (String name : pinMap.keySet()) {
            off(name);
        }
    }


    /**
        * Pause bedeutet, dass alle noch Pins die nicht mehr laufen
        * aus der schemes Map gelöscht werden. Die brauchen wir nicht mehr.
        * Danach wird der Executor umgehend beendet und alle Einträge gelöscht.
        */
       public void pause() {
           lock.lock();
           try {
               // save pause state
               for (String name : futures.keySet()) {
                   if (!futures.get(name).isDone()) { // but only if it runs
                       futures.get(name).cancel(true);
                   } else {
                       schemes.remove(name);
                   }
               }
               // es gibt kein Pause bei einem Executor
               executorService.shutdownNow();
           } finally {
               lock.unlock();
           }
       }

       /**
        * Resume erstellt einen NEUEN, LEEREN Executor und füllt diesen mit
        * allen Pins, die in der scheme Map stehen. Zu Beginn ist diese Map sowieso
        * leer. Ansonsten werden alle Pins "wiederbelebt", die zum Zeitpunkt der Pause noch
        * aktiv waren. Das Blink-Muster beginnt zwar von vorne, aber damit müssen wir jetzt leben.
        */
       public void resume() {
           // und auch kein Resume.
           lock.lock();
           try {
               executorService = Executors.newFixedThreadPool(20);

               for (String name : schemes.keySet()) {
                   GenericBlinkModel pinBlinkModel = pinMap.get(name);
                   pinBlinkModel.setScheme(schemes.get(name));
                   futures.put(name, executorService.submit(pinBlinkModel));
               }
           } finally {
               lock.unlock();
           }
       }

}
