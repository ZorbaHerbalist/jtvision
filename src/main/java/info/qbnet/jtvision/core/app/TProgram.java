package info.qbnet.jtvision.core.app;

import info.qbnet.jtvision.Console;
import info.qbnet.jtvision.backend.Backend;
import info.qbnet.jtvision.backend.factory.BackendFactoryProvider;
import info.qbnet.jtvision.backend.factory.BackendType;
import info.qbnet.jtvision.backend.factory.Factory;
import info.qbnet.jtvision.core.objects.TRect;
import info.qbnet.jtvision.core.views.TGroup;
import info.qbnet.jtvision.core.views.TPalette;
import info.qbnet.jtvision.util.Screen;

import java.awt.*;

import static info.qbnet.jtvision.core.views.TPalette.parseHexString;

public class TProgram extends TGroup {

    public static TProgram application = null;
    public static TDesktop desktop = null;

    private final Screen screen;
    private final Backend backend;
    private final Console console;

    public static final TPalette C_APP_COLOR = new TPalette(parseHexString(
            "\\x71\\x70\\x78\\x74\\x20\\x28\\x24\\x17\\x1F\\x1A" +
            "\\x31\\x31\\x1E\\x71\\x1F" +
            "\\x37\\x3F\\x3A\\x13\\x13\\x3E\\x21\\x3F\\x70\\x7F\\x7A\\x13\\x13\\x70\\x7F\\x7E" +
            "\\x70\\x7F\\x7A\\x13\\x13\\x70\\x70\\x7F\\x7E\\x20\\x2B\\x2F\\x78\\x2E\\x70\\x30" +
            "\\x3F\\x3E\\x1F\\x2F\\x1A\\x20\\x72\\x31\\x31\\x30\\x2F\\x3E\\x31\\x13\\x38\\x00" +
            "\\x17\\x1F\\x1A\\x71\\x71\\x1E\\x17\\x1F\\x1E\\x20\\x2B\\x2F\\x78\\x2E\\x10\\x30" +
            "\\x3F\\x3E\\x70\\x2F\\x7A\\x20\\x12\\x31\\x31\\x30\\x2F\\x3E\\x31\\x13\\x38\\x00" +
            "\\x37\\x3F\\x3A\\x13\\x13\\x3E\\x30\\x3F\\x3E\\x20\\x2B\\x2F\\x78\\x2E\\x30\\x70" +
            "\\x7F\\x7E\\x1F\\x2F\\x1A\\x20\\x32\\x31\\x71\\x70\\x2F\\x7E\\x71\\x13\\x38\\x00"
    ));

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

        application = this;

        this.state = State.SF_VISIBLE | State.SF_SELECTED | State.SF_FOCUSED | State.SF_MODAL | State.SF_EXPOSED;
        this.options = 0;
        this.buffer = screen;

        initDesktop();

        if (desktop != null) {
            insert(desktop);
        }
    }

    @Override
    public TPalette getPalette() {
        return C_APP_COLOR;
    }

    public void initDesktop() {
        TRect r = new TRect();
        getExtent(r);
        r.a.y++;
        r.b.y--;
        desktop = new TDesktop(r);
    }

    // Getters and setters

    /**
     * Returns the console instance created for this program.
     */
    public Console getConsole() {
        logger.trace("{} TProgram@getConsole()", getLogName());

        return console;
    }

}
