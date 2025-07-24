package info.qbnet.jtvision.backend.factory;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import info.qbnet.jtvision.backend.Backend;
import info.qbnet.jtvision.core.Screen;

import java.util.function.Function;

/**
 * Generic LibGDX backend factory accepting ApplicationAdapter constructor.
 */
public class LibGdxFactory implements Factory {

    private final Function<Screen, ApplicationAdapter> constructor;

    public LibGdxFactory(Function<Screen, ApplicationAdapter> constructor) {
        this.constructor = constructor;
    }

    @Override
    public void initialize() {
        // No-op for LibGDX
    }

    @Override
    public Backend createBackend(Screen buffer) {
        ApplicationAdapter app = constructor.apply(buffer);

        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "DOS Console (LibGDX)";
        config.width = buffer.getWidth() * 8;
        config.height = buffer.getHeight() * 16;

        new LwjglApplication(app, config);
        return new DummyBackend();
    }

    private static class DummyBackend implements Backend {
        @Override
        public void render() {}
    }
}
