package info.qbnet.jtvision.backend.factory;

import info.qbnet.jtvision.backend.Backend;
import info.qbnet.jtvision.backend.util.ThreadWatcher;
import info.qbnet.jtvision.core.Screen;

import java.util.concurrent.CountDownLatch;
import java.util.function.Function;

/**
 * Base class for GUI backend factories providing common utilities.
 */
public abstract class Factory<B extends Backend> {

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
        // no-op
    }

    /**
     * Creates and returns a rendering backend for the given screen buffer.
     *
     * @param screen the screen buffer
     * @return a rendering backend instance
     */
    public final B createBackend(Screen screen) {
        CountDownLatch latch = new CountDownLatch(1);
        B backend = constructor.apply(screen);
        Thread mainThread = Thread.currentThread();

        int pixelWidth = screen.getWidth() * backend.getCharWidth();
        int pixelHeight = screen.getHeight() * backend.getCharHeight();

        initializeBackend(backend, pixelWidth, pixelHeight, latch, mainThread);
        awaitInitialization(latch);
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
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
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
        ThreadWatcher.onTermination(mainThread, cleanupAction);
        Runtime.getRuntime().addShutdownHook(new Thread(cleanupAction));
    }
}
