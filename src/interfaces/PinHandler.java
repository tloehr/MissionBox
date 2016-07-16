package interfaces;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This handler runs parallel to the main programm and handles all the blinking needs of the specific pins.
 */
public class PinHandler {
    final ReentrantLock lock = new ReentrantLock();
    final HashMap<String, PinBlinkModel> pinMap = new HashMap<>();
    final HashMap<String, Future<String>> futures = new HashMap<>();
    final ExecutorService executor = Executors.newFixedThreadPool(8);

    public void add(Relay relay) {
        lock.lock();
        try {
            pinMap.put(relay.getName(), new PinBlinkModel(relay));
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
            if (pinBlinkModel != null && futures.containsKey(name) && !futures.get(name).isDone()) {
                futures.get(name).cancel(true);
            }
            pinBlinkModel.setScheme(scheme);
            futures.put(name, executor.submit(pinBlinkModel));
        } finally {
            lock.unlock();
        }
    }

}
