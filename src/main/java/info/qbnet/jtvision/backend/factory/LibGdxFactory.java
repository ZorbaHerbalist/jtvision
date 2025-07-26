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
public class LibGdxFactory extends AbstractGuiFactory<GuiComponent<ApplicationAdapter>> {

    public LibGdxFactory(Function<Screen, ? extends GuiComponent<ApplicationAdapter>> constructor) {
        super(constructor, "LibGDX");
    }

    @Override
    public GuiComponent<ApplicationAdapter> createBackend(Screen buffer) {
        Thread mainThread = Thread.currentThread();

        GuiComponent<ApplicationAdapter> backend = createAndInitialize(buffer, (b, latch) -> {
            WindowConfig config = createWindowConfig(b, buffer);
            
            // For LibGDX backends, we need to set the initialization latch
            if (b instanceof LibGdxBackendWithInitialization) {
                ((LibGdxBackendWithInitialization) b).setInitializationLatch(latch);
            }

            LwjglApplicationConfiguration lwjglConfig = new LwjglApplicationConfiguration();
            lwjglConfig.title = config.getTitle();
            lwjglConfig.width = config.getWidth();
            lwjglConfig.height = config.getHeight();

            ApplicationAdapter adapter = b.getNativeComponent();
            Thread uiThread = new Thread(() -> new LwjglApplication(adapter, lwjglConfig));
            uiThread.setDaemon(true);
            uiThread.start();
        });

        setupThreadCleanup(mainThread, () -> Gdx.app.postRunnable(() -> Gdx.app.exit()));

        return backend;
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
