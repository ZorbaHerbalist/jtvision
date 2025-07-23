package info.qbnet.jtvision;

import info.qbnet.jtvision.backend.*;

import java.awt.*;
import java.io.IOException;

public class TApplication {

    public TApplication() {
        System.out.println("Start");


        //BackendFactory backendFactory = new SwingBackendFactory(TTFSpriteFontBackend::new);
        //BackendFactory backendFactory = new JavaFxBackendFactory(TTFFontFxBackend::new);
        BackendFactory backendFactory =  new LibGdxBackendFactory(LibGdxTTFBackend::new);
        backendFactory.initialize();

        Screen screenBuffer = new Screen(80, 25, Color.LIGHT_GRAY, Color.BLACK);
        Backend backend = backendFactory.createBackend(screenBuffer);
        Console console = new Console(screenBuffer, backend);

        console.clearScreen();
        console.putString(10, 6, "Hello, DOS World!", Color.WHITE, Color.BLUE);
//        console.putString(10, 7, "Java Swing Emulator", Color.YELLOW, Color.RED);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        console.putString(10, 7, "Java Swing Emulator", Color.YELLOW, Color.RED);
    }
}
