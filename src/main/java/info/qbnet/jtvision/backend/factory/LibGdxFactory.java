package info.qbnet.jtvision.backend.factory;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import info.qbnet.jtvision.backend.Backend;
import info.qbnet.jtvision.core.Screen;

import java.util.concurrent.CountDownLatch;
import java.util.function.Function;

/**
 * Generic LibGDX backend factory using constructor injection.
 */
public class LibGdxFactory implements Factory {

    private final Function<Screen, ? extends LibGdxBackendWithAdapter> constructor;

    public LibGdxFactory(Function<Screen, ? extends LibGdxBackendWithAdapter> constructor) {
        this.constructor = constructor;
    }

    @Override
    public void initialize() {
        // No-op for LibGDX
    }

    @Override
    public Backend createBackend(Screen buffer) {
        LibGdxBackendWithAdapter backend = constructor.apply(buffer);

        CountDownLatch latch = new CountDownLatch(1);
        backend.setInitializationLatch(latch);

        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "Console (Library: LibGDX, Renderer: " + backend.getClass().getSimpleName() + ")";
        config.width = buffer.getWidth() * 8;
        config.height = buffer.getHeight() * 16;

        Thread thread = new Thread(() -> new LwjglApplication(
                backend.getApplicationAdapter(), config));
        thread.setDaemon(true);
        thread.start();

        // The LibGDX factory waits for backend initialization. A latch is set on the backend, the application
        // thread is started, and the factory blocks until initialization completes
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }

        return backend;
    }

    /**
     * Backend returned by this factory. Provides access to the
     * {@link ApplicationAdapter} required to start the LibGDX application.
     */
    public interface LibGdxBackendWithAdapter extends Backend {
        ApplicationAdapter getApplicationAdapter();

        /**
         * Sets a latch that will be counted down once the LibGDX application
         * has finished its initialization phase.
         */
        void setInitializationLatch(CountDownLatch latch);
    }
}
