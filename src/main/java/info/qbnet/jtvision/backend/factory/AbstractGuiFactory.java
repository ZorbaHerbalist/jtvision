package info.qbnet.jtvision.backend.factory;

import info.qbnet.jtvision.backend.Backend;
import info.qbnet.jtvision.core.Screen;

import java.util.concurrent.CountDownLatch;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Base class for GUI backend factories providing common utilities.
 */
public abstract class AbstractGuiFactory<B extends Backend> implements Factory<B> {

    private final Function<Screen, ? extends B> constructor;

    protected AbstractGuiFactory(Function<Screen, ? extends B> constructor) {
        this.constructor = constructor;
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
