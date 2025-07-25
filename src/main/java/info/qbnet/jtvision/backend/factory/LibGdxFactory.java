package info.qbnet.jtvision.backend.factory;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import info.qbnet.jtvision.backend.Backend;
import info.qbnet.jtvision.core.Screen;
import info.qbnet.jtvision.backend.util.ThreadWatcher;

import java.util.concurrent.CountDownLatch;
import java.util.function.Function;

/**
 * Generic LibGDX backend factory using constructor injection.
 */
public class LibGdxFactory extends AbstractGuiFactory<LibGdxFactory.LibGdxBackendWithAdapter> {

    public LibGdxFactory(Function<Screen, ? extends LibGdxBackendWithAdapter> constructor) {
        super(constructor);
    }

    @Override
    public Backend createBackend(Screen buffer) {
        CountDownLatch latch = new CountDownLatch(1);

        Thread mainThread = Thread.currentThread();

        LibGdxBackendWithAdapter backend = createBackendInstance(buffer);
        backend.setInitializationLatch(latch);

        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "Console (Library: LibGDX, Renderer: " + backend.getClass().getSimpleName() + ")";
        config.width = buffer.getWidth() * 8;
        config.height = buffer.getHeight() * 16;

        Thread uiThread = new Thread(() -> new LwjglApplication(
                backend.getApplicationAdapter(), config));
        uiThread.setDaemon(true);
        uiThread.start();

        // The LibGDX factory waits for backend initialization. A latch is set on the backend, the application
        // uiThread is started, and the factory blocks until initialization completes
        awaitInitialization(latch);

        Runnable exit = () -> Gdx.app.postRunnable(() -> Gdx.app.exit());
        ThreadWatcher.onTermination(mainThread, exit);
        Runtime.getRuntime().addShutdownHook(new Thread(exit));

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
