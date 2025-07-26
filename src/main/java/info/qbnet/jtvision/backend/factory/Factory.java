package info.qbnet.jtvision.backend.factory;

import info.qbnet.jtvision.backend.Backend;
import info.qbnet.jtvision.backend.CharDimensions;
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
        B backend = createBackendInstance(screen);
        Thread mainThread = Thread.currentThread();

        int pixelWidth = 0;
        int pixelHeight = 0;
        if (backend instanceof CharDimensions dims) {
            pixelWidth = screen.getWidth() * dims.getCharWidth();
            pixelHeight = screen.getHeight() * dims.getCharHeight();
        }

        initializeBackend(backend, pixelWidth, pixelHeight, latch, mainThread);
        awaitInitialization(latch);
        return backend;
    }

    /**
     * Create a backend instance using the stored constructor.
     */
    protected B createBackendInstance(Screen screen) {
        return constructor.apply(screen);
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
    
    /**
     * Creates a window configuration with common parameters.
     */
    protected FactoryConfig createFactoryConfig(B backend) {
        return new FactoryConfig(
            libraryName,
            backend.getClass().getSimpleName()
        );
    }
    
    /**
     * Sets up common thread cleanup for the main thread.
     */
    protected void setupThreadCleanup(Thread mainThread, Runnable cleanupAction) {
        ThreadWatcher.onTermination(mainThread, cleanupAction);
        Runtime.getRuntime().addShutdownHook(new Thread(cleanupAction));
    }
}
