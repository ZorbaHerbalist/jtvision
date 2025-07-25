package info.qbnet.jtvision.backend.factory;

import info.qbnet.jtvision.backend.Backend;
import info.qbnet.jtvision.core.Screen;

import java.util.concurrent.CountDownLatch;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Base class for GUI backend factories providing common utilities.
 * This class serves as both the factory interface and implementation base,
 * combining the functionality of the former Factory interface with concrete utilities.
 */
public abstract class AbstractGuiFactory<B extends Backend> {

    private final Function<Screen, ? extends B> constructor;

    protected AbstractGuiFactory(Function<Screen, ? extends B> constructor) {
        this.constructor = constructor;
    }

    /**
     * Creates and returns a rendering backend for the given screen buffer.
     * @param buffer the screen buffer
     * @return a rendering backend instance
     */
    public abstract B createBackend(Screen buffer);

    /**
     * Initializes any GUI-related setup before backend creation.
     *
     * <p>Default implementation performs no action. Factories that require
     * initialization can override this method.</p>
     */
    public void initialize() {
        // no-op
    }

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
}
