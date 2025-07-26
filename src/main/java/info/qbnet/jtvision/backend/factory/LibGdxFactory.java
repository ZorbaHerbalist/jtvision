package info.qbnet.jtvision.backend.factory;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import info.qbnet.jtvision.core.Screen;

import java.util.concurrent.CountDownLatch;
import java.util.function.Function;

/**
 * Generic LibGDX backend factory using constructor injection.
 */
public class LibGdxFactory extends Factory<GuiComponent<ApplicationAdapter>> {

    public LibGdxFactory(Function<Screen, ? extends GuiComponent<ApplicationAdapter>> constructor) {
        super(constructor, "LibGDX");
    }

    @Override
    protected void initializeBackend(GuiComponent<ApplicationAdapter> backend,
                                    int pixelWidth, int pixelHeight,
                                    CountDownLatch latch, Thread mainThread) {
        FactoryConfig config = createFactoryConfig(backend);

        if (backend instanceof LibGdxBackendWithInitialization initBackend) {
            initBackend.setInitializationLatch(latch);
        }

        LwjglApplicationConfiguration lwjglConfig = new LwjglApplicationConfiguration();
        lwjglConfig.title = config.getTitle();
        lwjglConfig.width = pixelWidth;
        lwjglConfig.height = pixelHeight;

        ApplicationAdapter adapter = backend.getNativeComponent();
        Thread uiThread = new Thread(() -> new LwjglApplication(adapter, lwjglConfig));
        uiThread.setDaemon(true);
        uiThread.start();

        setupThreadCleanup(mainThread, () -> Gdx.app.postRunnable(() -> Gdx.app.exit()));
    }

    /**
     * Interface for LibGDX backends that need initialization support.
     * This maintains compatibility with existing LibGDX backend implementations.
     */
    public interface LibGdxBackendWithInitialization {
        /**
         * Sets a latch that will be counted down once the LibGDX application
         * has finished its initialization phase.
         */
        void setInitializationLatch(CountDownLatch latch);
    }
}
