package info.qbnet.jtvdemo;

import info.qbnet.jtvision.Console;
import info.qbnet.jtvision.core.app.TApplication;
import info.qbnet.jtvision.backend.factory.BackendType;

import java.awt.*;

public class DemoApp extends TApplication {

    public DemoApp() {
        super(determineBackendType());

        Console console = getConsole();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        console.clearScreen();
        drawDoubleFrame(console, 2, 2, 76, 21, Color.WHITE, Color.BLACK);
        console.putString(10, 6, "Hello, DOS World!", Color.WHITE, Color.BLUE);
        console.flush();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        console.putString(10, 7, "Java Swing Emulator", Color.YELLOW, Color.RED);
        console.flush();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        // Measure how many characters per second we can print to the backend
        measureBackendSpeed(console);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        console.shutdown();
    }

    private static BackendType determineBackendType() {
        String backendTypeName = System.getProperty("console.backend", "SWING_BITMAP");
        try {
            return BackendType.valueOf(backendTypeName.toUpperCase());
        } catch (IllegalArgumentException ex) {
            System.err.println("Unknown backend: " + backendTypeName + ", using SWING_BITMAP");
            return BackendType.SWING_BITMAP;
        }
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

    public static void main(String[] args) {
        System.out.println("Hello world!");

        DemoApp app = new DemoApp();

        System.out.println("Finished!!!");
    }
}

