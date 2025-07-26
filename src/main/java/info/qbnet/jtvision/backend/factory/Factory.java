package info.qbnet.jtvision.backend.factory;

import info.qbnet.jtvision.backend.Backend;
import info.qbnet.jtvision.backend.util.ThreadWatcher;
import info.qbnet.jtvision.core.Screen;

import java.util.concurrent.CountDownLatch;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for GUI backend factories providing common utilities.
 */
public abstract class Factory<B extends Backend> {

    private static final Logger log = LoggerFactory.getLogger(Factory.class);

    private final Function<Screen, ? extends B> constructor;
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
        B backend = constructor.apply(screen);
        log.debug("Backend instance created: {}", backend.getClass().getSimpleName());

        CountDownLatch latch = new CountDownLatch(1);
        Thread mainThread = Thread.currentThread();

        int pixelWidth = screen.getWidth() * backend.getCharWidth();
        int pixelHeight = screen.getHeight() * backend.getCharHeight();
        log.debug("Computed window size: {}x{}", pixelWidth, pixelHeight);

        log.debug("Initializing backend...");
        initializeBackend(backend, pixelWidth, pixelHeight, latch, mainThread);
        awaitInitialization(latch);
        log.info("Backend initialized");
        return backend;
    }

    /**
     * Performs library specific initialization of the backend.
     *
     * @param backend      backend instance to initialize
     * @param pixelWidth   computed window width in pixels
     * @param pixelHeight  computed window height in pixels
     * @param latch        latch used to signal completion
     * @param mainThread   main thread creating the backend
     */
    protected abstract void initializeBackend(
            B backend, int pixelWidth, int pixelHeight,
            CountDownLatch latch, Thread mainThread);

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
