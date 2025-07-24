package info.qbnet.jtvision;

import info.qbnet.jtvision.backend.*;
import info.qbnet.jtvision.backend.factory.BackendFactoryProvider;
import info.qbnet.jtvision.backend.factory.BackendType;
import info.qbnet.jtvision.backend.factory.Factory;
import info.qbnet.jtvision.core.Screen;

import java.awt.*;

public class TApplication {

    public TApplication() {
        String backendName = System.getProperty("console.backend", "SWING_BITMAP");
        BackendType type;
        try {
            type = BackendType.valueOf(backendName.toUpperCase());
        } catch (IllegalArgumentException ex) {
            System.err.println("Unknown backend: " + backendName + ", using SWING_BITMAP");
            type = BackendType.SWING_BITMAP;
        }

        Factory factory = BackendFactoryProvider.getFactory(type);
        factory.initialize();

        Screen screenBuffer = new Screen(80, 25, Color.LIGHT_GRAY, Color.BLACK);
        Backend backend = factory.createBackend(screenBuffer);
        Console console = new Console(screenBuffer, backend);

        console.clearScreen();
        console.putString(10, 6, "Hello, DOS World!", Color.WHITE, Color.BLUE);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        console.putString(10, 7, "Java Swing Emulator", Color.YELLOW, Color.RED);
    }
}
