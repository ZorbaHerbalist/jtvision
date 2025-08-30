package info.qbnet.jtvdemo;

import info.qbnet.jtvision.Console;
import info.qbnet.jtvision.core.app.TApplication;
import info.qbnet.jtvision.backend.factory.BackendType;
import info.qbnet.jtvision.core.app.TProgram;
import info.qbnet.jtvision.core.constants.Command;
import info.qbnet.jtvision.core.constants.KeyCode;
import info.qbnet.jtvision.core.dialogs.*;
import info.qbnet.jtvision.core.event.TEvent;
import info.qbnet.jtvision.core.menus.TMenuBar;
import info.qbnet.jtvision.core.menus.TStatusDef;
import info.qbnet.jtvision.core.menus.TStatusItem;
import info.qbnet.jtvision.core.menus.TStatusLine;
import info.qbnet.jtvision.core.objects.TRect;
import info.qbnet.jtvision.core.views.TWindow;
import info.qbnet.jtvision.util.DataPacket;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DemoApp extends TApplication {

    public int winCount = 0;

    public static final int CM_HIDE_WINDOW = 101;
    public static final int CM_GREETINGS = 102;
    public static final int CM_ABOUT = 103;
    public static final int CM_DLG_INPUT_LINE = 104;
    public static final int CM_DLG_CURSOR = 105;

    private static final String FILE_TO_READ = "/demo.txt";
    private static final int MAX_LINES = 100;

    private final List<String> lines;

    public DemoApp() {
        super(determineBackendType());
        this.lines = readFile();

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

    private void doAboutBox() {
        //MsgBox.messageBox((char) 0x3 + "Tutorial Application\n" + (char) 0x3 + "Copyright (c) 2025\n"  + (char) 0x3 + "ZorbaHerbalist",  MsgBox.MF_INFORMATION + MsgBox.MF_OK_BUTTON);
        MsgBox.messageBox((char) 0x3 + "Tutorial Application\n" + (char) 0x3 + "Copyright (c) 2025\n"  + (char) 0x3 + "ZorbaHerbalist",  MsgBox.MF_INFORMATION + MsgBox.MF_OK_BUTTON);
    }

    private void doDlgInputLine() {
        TDialog d = new TDialog(new TRect(10, 5, 48, 12), "Input Line");

        TInputLine input = new TInputLine(new TRect(2, 2, 36, 3), 100);

        DataPacket defaults = new DataPacket(100)
                .putString("Not empty input line. Long texts are scrollable.")
                .rewind();
        ByteBuffer initBuf = defaults.getByteBuffer();
        int initLen = Short.toUnsignedInt(initBuf.getShort());
        ByteBuffer initSlice = initBuf.slice();
        initSlice.limit(initLen);
        input.setData(initSlice);
        d.insert(input);

        d.insert(new TButton(new TRect(8, 4, 18, 6), "~O~K", Command.CM_OK, TButton.BF_DEFAULT));
        d.insert(new TButton(new TRect(20, 4, 30, 6), "~C~ancel", Command.CM_CANCEL, 0));
        d.selectNext(false);

        if (desktop.execView(d) == Command.CM_OK) {
            DataPacket result = new DataPacket(input.dataSize());
            input.getData(result.getByteBuffer());
            String entered = new String(result.toByteArray(), StandardCharsets.UTF_8);
            MsgBox.messageBox("Entered string: " + entered, MsgBox.MF_INFORMATION + MsgBox.MF_OK_BUTTON);
        }
    }

    private void doDlgCursor() {
        TDialog d = new TDialog(new TRect(10, 5, 60, 20), "Cursor Demo");

        CursorDemoView view = new CursorDemoView(new TRect(2, 2, 38, 9));
        d.insert(view);

        d.insert(new TButton(new TRect(20, 10, 30, 12), "~O~K", Command.CM_OK, TButton.BF_DEFAULT));

        d.selectNext(false);

        desktop.execView(d);
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

    private List<String> readFile() {
        List<String> list = new ArrayList<>();
        try (InputStream is = DemoApp.class.getResourceAsStream(FILE_TO_READ);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null && list.size() < MAX_LINES) {
                list.add(line);
            }
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
        return list;
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

    public void greetingBox() {
        TRect r = new TRect(25, 5, 55, 16);
        TDialog d = new TDialog(r, "Hello, World!");

        r.assign(3, 5, 15, 6);
        d.insert(new TStaticText(r, "How are you?"));

        r.assign(16, 2, 28, 4);
        d.insert(new TButton(r, "Terrific", Command.CM_CANCEL, TButton.BF_NORMAL));

        r.assign(16, 4, 28, 6);
        d.insert(new TButton(r, "OK", Command.CM_CANCEL, TButton.BF_NORMAL));

        r.assign(16, 6, 28, 8);
        d.insert(new TButton(r, "Lousy", Command.CM_CANCEL, TButton.BF_NORMAL));

        r.assign(16, 8, 28, 10);
        d.insert(new TButton(r, "Cancel", Command.CM_CANCEL, TButton.BF_NORMAL));

        desktop.execView(d);
    }

    @Override
    public void initMenuBar() {
        TRect r = new TRect();
        getExtent(r);
        r.b.y = r.a.y + 1;
        menuBar = new TMenuBar(r, TMenuBar.newMenu(
                TMenuBar.newSubmenu("~" + (char) 0xF0 + "~", HelpContext.HC_NO_CONTEXT, TMenuBar.newMenu(
                        TMenuBar.newItem("~G~reetings", "", KeyCode.KB_NO_KEY, CM_GREETINGS, HelpContext.HC_NO_CONTEXT,
                        null)),
                TMenuBar.newSubmenu("~F~ile", 0, TMenuBar.newMenu(
                        TMenuBar.newItem("~N~ew", "", KeyCode.KB_NO_KEY, Command.CM_NEW, HelpContext.HC_NO_CONTEXT,
                        TMenuBar.newItem("~O~pen...", "F3", KeyCode.KB_F3, Command.CM_OPEN, HelpContext.HC_NO_CONTEXT,
                        TMenuBar.newLine(
                        TMenuBar.newItem("E~x~it", "Alt+X", KeyCode.KB_ALT_X, Command.CM_QUIT, HelpContext.HC_NO_CONTEXT,
                        null))))),
                TMenuBar.newSubmenu("~D~ialog", 0, TMenuBar.newMenu(
                        TMenuBar.newItem("~C~ursor demo", null, KeyCode.KB_NO_KEY, CM_DLG_CURSOR, HelpContext.HC_NO_CONTEXT,
                        TMenuBar.newItem("~I~nput line", null, KeyCode.KB_NO_KEY, CM_DLG_INPUT_LINE, HelpContext.HC_NO_CONTEXT,
                        null))),
                TMenuBar.newSubmenu("~W~indow", 0, TMenuBar.newMenu(
                        TMenuBar.newItem("~N~ext", "F6", KeyCode.KB_F6, Command.CM_NEXT, HelpContext.HC_NO_CONTEXT,
                        TMenuBar.newItem("~Z~oom", "F5", KeyCode.KB_F5, Command.CM_ZOOM, HelpContext.HC_NO_CONTEXT,
                        null))),
                TMenuBar.newSubmenu("~H~elp", 0, TMenuBar.newMenu(
                        TMenuBar.newItem("~A~bout", "", KeyCode.KB_NO_KEY, CM_ABOUT, HelpContext.HC_NO_CONTEXT,
                        null)),
                null)))))));
    }

    @Override
    public void handleEvent(TEvent event) {
        super.handleEvent(event);
        if (event.what == TEvent.EV_COMMAND) {
            switch (event.msg.command) {
                case Command.CM_NEW:
                    newWindow();
                    break;
                case CM_GREETINGS:
                    greetingBox();
                    break;
                case CM_ABOUT:
                    doAboutBox();
                    break;
                case CM_DLG_INPUT_LINE:
                    doDlgInputLine();
                    break;
                case CM_DLG_CURSOR:
                    doDlgCursor();
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
                        new TStatusItem("~F4~ New", KeyCode.KB_F4, Command.CM_NEW,
                        new TStatusItem("Cascade", KeyCode.KB_NO_KEY, Command.CM_CASCADE,
                        new TStatusItem("Tile", KeyCode.KB_NO_KEY, Command.CM_TILE,
                        TProgram.stdStatusKeys(null)))),
                null));
    }

    public void newWindow() {
        winCount++;
        TRect r = new TRect(0, 0, 26, 7);
        Random rand = new Random();
        r.move(rand.nextInt(58), rand.nextInt(16));
        TWindow window = new DemoWindow(r, "Demo Window", winCount, lines);
        desktop.insert(window);
    }

    public static void main(String[] args) {
        System.out.println("Hello world!");

        DemoApp app = new DemoApp();
        app.run();

        System.out.println("Finished!!!");
    }
}

