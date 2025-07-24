package info.qbnet.jtvision;

import info.qbnet.jtvision.backend.*;
import info.qbnet.jtvision.backend.factory.Factory;
import info.qbnet.jtvision.backend.factory.LibGdxFactory;
import info.qbnet.jtvision.backend.factory.SwingFactory;
import info.qbnet.jtvision.core.Screen;

import java.awt.*;

public class TApplication {

    public TApplication() {
        System.out.println("Start");


        //Factory factory1 = new SwingFactory(SwingTrueTypeBackend::new);
        //Factory factory1 = new JavaFxFactory(JavaFxBitmapBackend::new);
        Factory factory1 = new LibGdxFactory(LibGdxBitmapBackend::new);
        factory1.initialize();

//        Factory factory2 = new SwingFactory(SwingTrueTypeBackend::new);
//        factory2.initialize();

        Screen screenBuffer = new Screen(80, 25, Color.LIGHT_GRAY, Color.BLACK);
        Backend backend1 = factory1.createBackend(screenBuffer);
        Console console1 = new Console(screenBuffer, backend1);

//        Backend backend2 = factory2.createBackend(screenBuffer);
//        Console console2 = new Console(screenBuffer, backend2);


        console1.clearScreen();
        console1.putString(10, 6, "Hello, DOS World!", Color.WHITE, Color.BLUE);
//        console.putString(10, 7, "Java Swing Emulator", Color.YELLOW, Color.RED);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        console1.putString(10, 7, "Java Swing Emulator", Color.YELLOW, Color.RED);
    }
}
