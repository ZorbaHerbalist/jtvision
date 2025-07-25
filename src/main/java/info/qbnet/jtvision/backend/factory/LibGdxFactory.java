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
    public LibGdxBackendWithAdapter createBackend(Screen buffer) {
        Thread mainThread = Thread.currentThread();

        LibGdxBackendWithAdapter backend = createAndInitialize(buffer, (b, latch) -> {
            b.setInitializationLatch(latch);

            LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
            config.title = "Console (Library: LibGDX, Renderer: " +
                    b.getClass().getSimpleName() + ")";
            config.width = buffer.getWidth() * 8;
            config.height = buffer.getHeight() * 16;

            Thread uiThread = new Thread(() -> new LwjglApplication(
                    b.getApplicationAdapter(), config));
            uiThread.setDaemon(true);
            uiThread.start();
        });

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
