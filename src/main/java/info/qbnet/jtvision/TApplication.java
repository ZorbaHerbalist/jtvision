package info.qbnet.jtvision;

import info.qbnet.jtvision.backend.*;

import java.awt.*;

public class TApplication {

    public TApplication() {
        System.out.println("Start");

        BackendFactory backendFactory = new TTFSpriteFontBackendFactory();
        backendFactory.initialize();

        Screen screenBuffer = new Screen(80, 25, Color.LIGHT_GRAY, Color.BLACK);
        Backend backend = backendFactory.createBackend(screenBuffer);
        Console console = new Console(screenBuffer, backend);

        console.clearScreen();
        console.putString(10, 5, "Hello, DOS World!", Color.WHITE, Color.BLUE);
        console.putString(10, 7, "Java Swing Emulator", Color.YELLOW, Color.RED);
    }
}
