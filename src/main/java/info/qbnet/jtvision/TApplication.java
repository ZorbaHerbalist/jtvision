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

        Factory<? extends Backend> factory = BackendFactoryProvider.getFactory(type);
        factory.initialize();

        Screen screenBuffer = new Screen(80, 25, Color.LIGHT_GRAY, Color.BLACK);
        Backend backend = factory.createBackend(screenBuffer);
        Console console = new Console(screenBuffer, backend);

        console.clearScreen();
        drawDoubleFrame(console, 2, 2, 76, 21, Color.WHITE, Color.BLACK);
        console.putString(10, 6, "Hello, DOS World!", Color.WHITE, Color.BLUE);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        console.putString(10, 7, "Java Swing Emulator", Color.YELLOW, Color.RED);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        // Measure how many characters per second we can print to the backend
        //measureBackendSpeed(console);
    }

    private void measureBackendSpeed(Console console) {
        final int iterations = 1000;
        final String testString = "X";
        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            console.putString(0, 0, testString, Color.WHITE, Color.BLACK);
        }
        long endTime = System.nanoTime();
        double durationSeconds = (endTime - startTime) / 1_000_000_000.0;
        double cps = iterations / durationSeconds;
        System.out.printf("Backend speed: %.2f chars/s%n", cps);
    }

    private void drawDoubleFrame(Console console, int x, int y, int width, int height,
                                 Color fg, Color bg) {
        if (width < 2 || height < 2) {
            return;
        }

        String horizontal = ("" + (char)205).repeat(width - 2);
        console.putString(x, y, (char)201 + horizontal + (char)187, fg, bg);

        for (int i = 1; i < height - 1; i++) {
            console.putString(x, y + i, (char)186 + " ".repeat(width - 2) + (char)186, fg, bg);
        }

        console.putString(x, y + height - 1, (char)200 + horizontal + (char)188, fg, bg);
    }
}
