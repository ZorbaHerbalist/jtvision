package info.qbnet.jtvision.backend.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Utility for executing an action when a given thread terminates.
 * Uses a daemon {@link ExecutorService} so callers don't need to
 * manually manage watcher threads.
 */
public final class ThreadWatcher {

    private static final ExecutorService WATCHER = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "ThreadWatcher");
        t.setDaemon(true);
        return t;
    });

    private ThreadWatcher() {
    }

    /**
     * Executes {@code onTerminate} once {@code thread} terminates.
     */
    public static void onTermination(Thread thread, Runnable onTerminate) {
        WATCHER.submit(() -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            onTerminate.run();
        });
    }

    /**
     * Shuts down the internal watcher service.
     */
    public static void shutdown() {
        WATCHER.shutdownNow();
    }
}
