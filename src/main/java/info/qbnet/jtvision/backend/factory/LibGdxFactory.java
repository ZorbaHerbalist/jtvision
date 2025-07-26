package info.qbnet.jtvision.backend.factory;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import info.qbnet.jtvision.backend.AbstractLibGdxBackend;
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

        if (backend instanceof AbstractLibGdxBackend initBackend) {
            initBackend.setInitializationLatch(latch);
        }

        LwjglApplicationConfiguration lwjglConfig = new LwjglApplicationConfiguration();
        lwjglConfig.title = createWindowTitle(backend);
        lwjglConfig.width = pixelWidth;
        lwjglConfig.height = pixelHeight;

        ApplicationAdapter adapter = backend.getNativeComponent();
        Thread uiThread = new Thread(() -> new LwjglApplication(adapter, lwjglConfig));
        uiThread.setDaemon(true);
        uiThread.start();

        setupThreadCleanup(mainThread, () -> Gdx.app.postRunnable(() -> Gdx.app.exit()));
    }
}
