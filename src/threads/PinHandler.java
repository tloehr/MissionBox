package threads;

import interfaces.Relay;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This handler runs parallel to the main programm and handles all the blinking needs of the specific pins. It knows to handle collision between pins that must not be run at the same time.
 * Dieser Handler läuft parallel zum Hauptprogramm. Er steuert alles Relais und achtet auch auf widersprüchliche Befehle und Kollisionen (falls bestimmte Relais nicht gleichzeitig anziehen dürfen, gibt mittlerweile nicht mehr).
 */

public class PinHandler {

    final Logger logger;
    final ReentrantLock lock;
    final HashMap<String, PinBlinkModel> pinMap;
    final HashMap<String, Future<String>> futures;
    final HashMap<String, String> schemes;
    private ExecutorService executor;
    boolean paused = false;

    /**
     * there relays that can be used at the same time. but others demand, that only *one* relay is used at the time (out of a set of relays). The sirens for instance.
     * Out of the 6 different signals that can be activated, ONLY ONE can be used at the time. If you activate more than two, the results are unpredictable.
     * So this thread makes sure, that only one is used at every point in time.
     * <p>
     * the led are also handled via the MCP23017, but they are connected directly to the attached darlington array. They can all be savely used at the same time.
     * <p>
     * This map assigns a collision domain to the specific relay (or pin) names.
     */
    final HashMap<String, Integer> collisionDomain;
    final HashMap<Integer, Set<String>> collisionDomainReverse; // helper map to ease the finding process

    public PinHandler() {
        lock = new ReentrantLock();
        pinMap = new HashMap<>();
        futures = new HashMap<>();
        schemes = new HashMap<>();
        collisionDomain = new HashMap<>();
        collisionDomainReverse = new HashMap<>();
        logger = Logger.getLogger(getClass());
        resume();
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
            executor.shutdownNow();
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
            executor = Executors.newFixedThreadPool(12);
            for (String name : schemes.keySet()) {
                PinBlinkModel pinBlinkModel = pinMap.get(name);
                pinBlinkModel.setScheme(schemes.get(name));
                futures.put(name, executor.submit(pinBlinkModel));
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * add the relay but don't care about collision domains.
     *
     * @param relay
     */
    public void add(Relay relay) {
        add(0, relay);
    }

    /**
     * adds a a relay to the handler.
     *
     * @param cd    (collisionDomain) das gibt es nur, wenn bestimmte Pins nicht zusammen verwendet werden dürfen. Hab ich im
     *              Moment nicht. cd == 0 bedeutet, dass dieser Pin machen kann was er will.
     * @param relay das betreffende Relais oder Pin.
     */
    public void add(int cd, Relay relay) {
        lock.lock();
        try {
            pinMap.put(relay.getName(), new PinBlinkModel(relay));
            if (cd > 0) {
                collisionDomain.put(relay.getName(), cd);
                add2ReverseMap(cd, relay.getName());
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Das Relais wird abgeschaltet.
     *
     * @param name
     */
    public void off(String name) {
        setScheme(name, "0;");
    }

    /**
     * schaltet das Relais ein und lässt es an.
     *
     * @param name
     */
    public void on(String name) {
        setScheme(name, "1;" + Long.MAX_VALUE + ",0");
    }

    /**
     * Setzt ein Blink Schema für dieses Relais. Die Syntax ist wie folgt:
     * "<anzahl_wiederholung/>;(millis-on;millis-off)*", wobei ()* bedeutet, dass diese Sequenz so oft wie
     * gewünscht wiederholt werden kann. Danach wird die Gesamtheit <anzahl_wiederholungen/> mal wiederholt
     * wird. Unendliche Wiederholungen werden einfach durch Long.MAX_VALUE
     *
     * @param name
     * @param scheme
     */
    public void setScheme(String name, String scheme) {
        lock.lock();
        try {
            PinBlinkModel pinBlinkModel = pinMap.get(name);
            if (pinBlinkModel != null) {
                int cd = collisionDomain.containsKey(name) ? collisionDomain.get(name) : 0; // 0 means NO collision domain
                if (cd > 0) { // we need to terminate a potentially running thread within this domain.
                    // get all the potentially colliding relays and check them.
                    for (String collidingName : collisionDomainReverse.get(cd)) {
                        if (futures.containsKey(collidingName) && !futures.get(collidingName).isDone()) { // but only if it runs
//                            logger.debug("terminating: " + collidingName + ": colliding with " + (collidingName.equals(name) ? ">>itself<<" : name));
                            futures.get(collidingName).cancel(true);
                        }
                    }
                } else {
                    if (futures.containsKey(name) && !futures.get(name).isDone()) { // but only if it runs
                        futures.get(name).cancel(true);
                    }
                }
                schemes.put(name, scheme); // aufbewahren für die Wiederherstellung nach der Pause
            }

            pinBlinkModel.setScheme(scheme);
            futures.put(name, executor.submit(pinBlinkModel));
        } catch (Exception e) {
            logger.fatal(e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * adds the cd entry to the reverse map. initializes the subset if necessary
     *
     * @param cd
     * @param name
     */
    private void add2ReverseMap(int cd, String name) {
        // no locking necessary, because the calling method is thread safe.
        if (!collisionDomainReverse.containsKey(cd)) {
            collisionDomainReverse.put(cd, new HashSet<>());
        }
        collisionDomainReverse.get(cd).add(name);
    }

    public void off() {
        for (String name : pinMap.keySet()) {
            off(name);
        }
    }

}
