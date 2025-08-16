package info.qbnet.jtvdemo;

import info.qbnet.jtvision.Console;
import info.qbnet.jtvision.core.app.TApplication;
import info.qbnet.jtvision.backend.factory.BackendType;
import info.qbnet.jtvision.core.constants.Command;
import info.qbnet.jtvision.core.constants.KeyCode;
import info.qbnet.jtvision.core.event.TEvent;
import info.qbnet.jtvision.core.menus.TMenuBar;
import info.qbnet.jtvision.core.menus.TStatusDef;
import info.qbnet.jtvision.core.menus.TStatusItem;
import info.qbnet.jtvision.core.menus.TStatusLine;
import info.qbnet.jtvision.core.objects.TRect;
import info.qbnet.jtvision.core.views.TWindow;

import java.awt.*;
import java.util.Random;

public class DemoApp extends TApplication {

    public int winCount = 0;

    public DemoApp() {
        super(determineBackendType());

//        Console console = getConsole();
        //console.putString(1, 1, "X", Color.WHITE, Color.BLACK);

//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        }
//
//        console.clearScreen();
//        drawDoubleFrame(console, 2, 2, 76, 21, Color.WHITE, Color.BLACK);
//        console.putString(10, 6, "Hello, DOS World!", Color.WHITE, Color.BLUE);
//        console.flush();
//
//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        }
//
//        console.putString(10, 7, "Java Swing Emulator", Color.YELLOW, Color.RED);
//        console.flush();
//
//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        }
//        // Measure how many characters per second we can print to the backend
//        measureBackendSpeed(console);
//
//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        }
//
//        console.shutdown();
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

    @Override
    public void initMenuBar() {
        TRect r = new TRect();
        getExtent(r);
        r.b.y = r.a.y + 1;
        menuBar = new TMenuBar(r, TMenuBar.newMenu(
                TMenuBar.newSubmenu("~F~ile", 0, TMenuBar.newMenu(
                        TMenuBar.newItem("~N~ew", "", KeyCode.KB_NO_KEY, Command.CM_NEW, HelpContext.HC_NO_CONTEXT,
                        TMenuBar.newItem("~O~pen...", "F3", KeyCode.KB_F3, Command.CM_OPEN, HelpContext.HC_NO_CONTEXT,
                        TMenuBar.newLine(
                        TMenuBar.newItem("E~x~it", "Alt+X", KeyCode.KB_ALT_X, Command.CM_QUIT, HelpContext.HC_NO_CONTEXT,
                        null))))),
                TMenuBar.newSubmenu("~W~indow", 0, TMenuBar.newMenu(
                        TMenuBar.newItem("~N~ext", "F6", KeyCode.KB_F6, Command.CM_NEXT, HelpContext.HC_NO_CONTEXT,
                        TMenuBar.newItem("~Z~oom", "F5", KeyCode.KB_F5, Command.CM_ZOOM, HelpContext.HC_NO_CONTEXT,
                        null))),
                null))
        ));
    }

    @Override
    public void handleEvent(TEvent event) {
        super.handleEvent(event);
        if (event.what == TEvent.EV_COMMAND) {
            switch (event.msg.command) {
                case Command.CM_NEW:
                    newWindow();
                    break;
                default:
                    return;
            }
            clearEvent(event);
        }
    }

    @Override
    public void initStatusLine() {
        TRect r = new TRect();
        getExtent(r);
        r.a.y = r.b.y - 1;
        statusLine = new TStatusLine(r,
                new TStatusDef(0, 0xFFFF,
                        new TStatusItem(null, KeyCode.KB_F10, Command.CM_MENU,
                        new TStatusItem("~Alt-X~ Exit", KeyCode.KB_ALT_X, Command.CM_QUIT,
                        new TStatusItem("~F4~ New", KeyCode.KB_F4, Command.CM_NEW,
                        new TStatusItem("~Alt-F3~ Close", KeyCode.KB_ALT_F3, Command.CM_CLOSE,
                        new TStatusItem("~F5~ Zoom", KeyCode.KB_F5, Command.CM_ZOOM,
                        null))))),
                null));
    }

    public void newWindow() {
        winCount++;
        TRect r = new TRect(0, 0, 26, 7);
        Random rand = new Random();
        r.move(rand.nextInt(58), rand.nextInt(16));
        TWindow window = new TWindow(r, "Demo Window", winCount);
        desktop.insert(window);
    }

    public static void main(String[] args) {
        System.out.println("Hello world!");

        DemoApp app = new DemoApp();
        app.run();

        System.out.println("Finished!!!");
    }
}

