package info.qbnet.jtvision.backend.factory;

import info.qbnet.jtvision.backend.Backend;
import info.qbnet.jtvision.backend.util.ThreadWatcher;
import info.qbnet.jtvision.core.Screen;

import java.util.concurrent.CountDownLatch;
import java.util.function.BiConsumer;
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
     * @param buffer the screen buffer
     * @return a rendering backend instance
     */
    public abstract B createBackend(Screen buffer);

    /**
     * Create a backend instance using the stored constructor.
     */
    protected B createBackendInstance(Screen screen) {
        return constructor.apply(screen);
    }

    protected B createAndInitialize(Screen screen, BiConsumer<B, CountDownLatch> initializer) {
        CountDownLatch latch = new CountDownLatch(1);
        B backend = createBackendInstance(screen);
        initializer.accept(backend, latch);
        awaitInitialization(latch);
        return backend;
    }

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
