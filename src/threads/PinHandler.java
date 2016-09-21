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
 */
public class PinHandler {

    final Logger logger;
    final ReentrantLock lock;
    final HashMap<String, PinBlinkModel> pinMap;
    final HashMap<String, Future<String>> futures;
    final ExecutorService executor;


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
        executor = Executors.newFixedThreadPool(12);
        collisionDomain = new HashMap<>();
        collisionDomainReverse = new HashMap<>();
        logger = Logger.getLogger(getClass());
    }


    /**
     * add the relay but don't care about collision domains.
     * @param relay
     */
    public void add(Relay relay) {
        add(0, relay);
    }

    /**
     * adds a a relay to the handler.
     *
     * @param cd    (collisionDomain) this is the collision domain to which this relay is to be assigned. a cd < 1 means, that that no cd is used for the relay, and that
     *              it can be safely used at any given time.
     * @param relay the relay to be handled
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

    public void off(String name) {
        setScheme(name, "0;");
    }

    public void on(String name) {
        setScheme(name, "1;" + Long.MAX_VALUE + ",0");
    }

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
                            logger.debug("terminating: " + collidingName + ": colliding with " + (collidingName.equals(name) ? ">>itself<<" : name));
                            futures.get(collidingName).cancel(true);
                        }
                    }
                } else {
                    if (futures.containsKey(name) && !futures.get(name).isDone()) { // but only if it runs
//                        logger.debug("terminating: " + name + ": was already running");
                        futures.get(name).cancel(true);
                    }
                }
            }

            pinBlinkModel.setScheme(scheme);
            futures.put(name, executor.submit(pinBlinkModel));
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
    void add2ReverseMap(int cd, String name) {
        // no locking necessary, because the calling method is thread safe.
        if (!collisionDomainReverse.containsKey(cd)) {
            collisionDomainReverse.put(cd, new HashSet<>());
        }
        collisionDomainReverse.get(cd).add(name);
    }

    public void off(){
        for (String name : pinMap.keySet()){
            off(name);
        }
    }

}
