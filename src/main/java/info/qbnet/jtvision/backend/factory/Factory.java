package info.qbnet.jtvision.backend.factory;

import info.qbnet.jtvision.backend.Backend;
import info.qbnet.jtvision.backend.util.ThreadWatcher;
import info.qbnet.jtvision.util.Screen;

import java.util.concurrent.CountDownLatch;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for GUI backend factories providing common utilities.
 */
public abstract class Factory<B extends Backend> {

    private static final Logger log = LoggerFactory.getLogger(Factory.class);

    /**
     * Constructs and initializes a GUI backend instance for a given screen.
     * This function is intended to be supplied by the implementing factory to facilitate
     * the creation of a rendering backend tailored to a specific framework or library.
     * <p>
     * The constructor function accepts a {@link Screen} object as input and returns a backend
     * instance of type {@code B}. It allows the factory to delegate the instantiation logic
     * to a custom implementation, which may vary depending on the graphical framework being used.
     * <p>
     * It is protected so subclasses can directly create the backend when needed,
     * e.g. on a dedicated UI thread.
     */
    protected final Function<Screen, ? extends B> constructor;
    private final String libraryName;

    protected Factory(Function<Screen, ? extends B> constructor, String libraryName) {
        this.constructor = constructor;
        this.libraryName = libraryName;
    }

    /**
     * Initializes any GUI-related setup before backend creation.
     * <p>
     * Default implementation performs no action. Subclasses may override
     * this method if initialization is required.
     * </p>
     */
    public void initialize() {
        log.debug("No initialization required for {}", libraryName);
    }

    /**
     * Creates and returns a rendering backend for the given screen buffer.
     *
     * @param screen the screen buffer
     * @return a rendering backend instance
     */
    public final B createBackend(Screen screen) {
        log.info("Creating backend using library {}", libraryName);

        CountDownLatch latch = new CountDownLatch(1);
        Thread mainThread = Thread.currentThread();

        log.debug("Initializing backend...");
        B backend = initializeBackend(screen, latch, mainThread);
        awaitInitialization(latch);
        log.info("Backend initialized");
        return backend;
    }

    /**
     * Performs library specific initialization of the backend.
     * Implementations are expected to create the backend instance and
     * signal completion on the provided latch once the UI is ready.
     *
     * @param screen      screen buffer for the backend
     * @param latch       latch used to signal completion
     * @param mainThread  main thread creating the backend
     */
    protected abstract B initializeBackend(
            Screen screen, CountDownLatch latch, Thread mainThread);

    /**
     * Wait for initialization to complete and handle interruptions.
     */
    protected void awaitInitialization(CountDownLatch latch) {
        try {
            log.debug("Waiting for UI initialization to complete... ");
            latch.await();
            log.debug("UI initialization completed");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Initialization interrupted", e);
            throw new RuntimeException(e);
        }
    }

    protected String createWindowTitle(B backend) {
        return String.format("Console (Library: %s, Renderer: %s)",
                libraryName, backend.getClass().getSimpleName());
    }
    
    /**
     * Sets up common thread cleanup for the main thread.
     */
    protected void setupThreadCleanup(Thread mainThread, Runnable cleanupAction) {
        log.debug("Registering cleanup actions for main thread");
        ThreadWatcher.onTermination(mainThread, cleanupAction);
        Runtime.getRuntime().addShutdownHook(new Thread(cleanupAction, "shutdown"));
    }
}
