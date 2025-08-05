package info.qbnet.jtvision.core.app;

import info.qbnet.jtvision.Console;
import info.qbnet.jtvision.backend.Backend;
import info.qbnet.jtvision.backend.factory.BackendFactoryProvider;
import info.qbnet.jtvision.backend.factory.BackendType;
import info.qbnet.jtvision.backend.factory.Factory;
import info.qbnet.jtvision.core.objects.TRect;
import info.qbnet.jtvision.core.views.TGroup;
import info.qbnet.jtvision.util.Screen;

import java.awt.*;

public class TProgram extends TGroup {

    private final Screen screen;
    private final Backend backend;
    private final Console console;

    /**
     * Creates a new program using the specified backend.
     *
     * @param type the backend type used to render the console
     */
    public TProgram(BackendType type) {
        super(new TRect(0, 0, 80, 25));

        logger.debug("{} TProgram@TProgram(type={})", getLogName(), type);

        Factory<? extends Backend> factory = BackendFactoryProvider.getFactory(type);
        factory.initialize();

        this.screen = new Screen(80, 25, Color.LIGHT_GRAY, Color.BLACK);
        this.backend = factory.createBackend(screen);
        this.console = new Console(screen, backend);

        setBuffer(screen);
    }

    /**
     * Returns the console instance created for this program.
     */
    public Console getConsole() {
        logger.trace("{} TProgram@getConsole()", getLogName());

        return console;
    }

}
