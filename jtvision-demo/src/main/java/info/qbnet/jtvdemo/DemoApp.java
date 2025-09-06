package info.qbnet.jtvdemo;

import info.qbnet.jtvision.util.Console;
import info.qbnet.jtvision.views.TApplication;
import info.qbnet.jtvision.backend.factory.BackendType;
import info.qbnet.jtvision.views.TProgram;
import info.qbnet.jtvision.util.Command;
import info.qbnet.jtvision.util.KeyCode;
import info.qbnet.jtvision.event.TEvent;
import info.qbnet.jtvision.views.TMenuBar;
import info.qbnet.jtvision.util.TStatusDef;
import info.qbnet.jtvision.util.TStatusItem;
import info.qbnet.jtvision.views.TStatusLine;
import info.qbnet.jtvision.util.TRect;
import info.qbnet.jtvision.util.MsgBox;
import info.qbnet.jtvision.views.*;
import info.qbnet.jtvision.util.TStream;
import info.qbnet.jtvision.util.SerializationRegistry;
import info.qbnet.jtvision.util.DataPacket;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class DemoApp extends TApplication {

    public int winCount = 0;

    public static final int CM_HIDE_WINDOW = 101;
    public static final int CM_GREETINGS = 102;
    public static final int CM_ABOUT = 103;
    public static final int CM_DLG_INPUT_LINE = 104;
    public static final int CM_DLG_CURSOR = 105;
    public static final int CM_DLG_FILE = 106;
    public static final int CM_DLG_RADIO_BUTTONS = 107;
    public static final int CM_DLG_CHECK_BOXES = 108;

    private static final String FILE_TO_READ = "/demo.txt";
    private static final String SAMPLE_DIALOG_FILE = "/sampleDialog.bin";
    private static final int MAX_LINES = 100;

    private final List<String> lines;

    public DemoApp() {
        super(determineBackendType());
        registerSerializableViews();
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

    /**
     * Ensures that core view classes are registered for deserialization.
     */
    static void registerSerializableViews() {
        SerializationRegistry.initCoreTypes();
    }

    private void doAboutBox() {
        //MsgBox.messageBox((char) 0x3 + "Tutorial Application\n" + (char) 0x3 + "Copyright (c) 2025\n"  + (char) 0x3 + "ZorbaHerbalist",  MsgBox.MF_INFORMATION + MsgBox.MF_OK_BUTTON);
        MsgBox.messageBox((char) 0x3 + "Tutorial Application\n" + (char) 0x3 + "Copyright (c) 2025\n"  + (char) 0x3 + "ZorbaHerbalist",  MsgBox.MF_INFORMATION + MsgBox.MF_OK_BUTTON);
    }

    private void doDlgInputLine() {
        TInputLine input = null;


        TDialog d = new TDialog(new TRect(10, 5, 48, 13), "Input Line");

        input = new TInputLine(new TRect(2, 3, 36, 4), 100);

        DataPacket defaults = new DataPacket(100)
                .putString("Not empty input line. Long texts are scrollable.")
                .rewind();
        ByteBuffer initBuf = defaults.getByteBuffer();
        int initLen = Short.toUnsignedInt(initBuf.getShort());
        ByteBuffer initSlice = initBuf.slice();
        initSlice.limit(initLen);
        input.setData(initSlice);
        d.insert(input);
        d.insert(new TLabel(new TRect(2, 2, 36, 3), "~T~ext:", input));
        d.insert(new TButton(new TRect(8, 5, 18, 7), "~O~K", Command.CM_OK, TButton.BF_DEFAULT));
        d.insert(new TButton(new TRect(20, 5, 30, 7), "~C~ancel", Command.CM_CANCEL, 0));
        d.selectNext(false);

        if (desktop.execView(d) == Command.CM_OK && input != null) {
            DataPacket result = new DataPacket(input.dataSize());
            input.getData(result.getByteBuffer());
            String entered = new String(result.toByteArray(), StandardCharsets.UTF_8);
            MsgBox.messageBox("Entered string: " + entered, MsgBox.MF_INFORMATION + MsgBox.MF_OK_BUTTON);
        }
    }

    private void doDlgRadioButtons() {
        TDialog d = new TDialog(new TRect(10, 6, 70, 18), "Radio buttons");

        TRadioButtons radio = new TRadioButtons(new TRect(3, 3, 35, 9),
                Arrays.asList("Option ~1~", "Option ~2~", "Option ~3~"));
        d.insert(radio);

        d.insert(new TLabel(new TRect(3, 2, 20, 3), "Choose an option:", radio));
        d.insert(new TButton(new TRect(38, 3, 48, 5), "~O~K", Command.CM_OK, TButton.BF_DEFAULT));
        d.insert(new TButton(new TRect(38, 6, 48, 8), "~C~ancel", Command.CM_CANCEL, 0));
        d.selectNext(false);

        if (desktop.execView(d) == Command.CM_OK) {
            int selected = radio.value;
            MsgBox.messageBox("Selected value: " + selected, MsgBox.MF_INFORMATION + MsgBox.MF_OK_BUTTON);
        }
    }

    private void doDlgCheckBoxes() {
        TDialog d = new TDialog(new TRect(10, 6, 70, 18), "Check boxes");

        TCheckBoxes checkBoxes = new TCheckBoxes(new TRect(3, 3, 35, 9),
                Arrays.asList("Option ~1~", "Option ~2~", "Option ~3~"));
        d.insert(checkBoxes);

        d.insert(new TLabel(new TRect(3, 2, 20, 3), "Choose an option:", checkBoxes));
        d.insert(new TButton(new TRect(38, 3, 48, 5), "~O~K", Command.CM_OK, TButton.BF_DEFAULT));
        d.insert(new TButton(new TRect(38, 6, 48, 8), "~C~ancel", Command.CM_CANCEL, 0));
        d.selectNext(false);

        if (desktop.execView(d) == Command.CM_OK) {
            int selected = checkBoxes.value;
            MsgBox.messageBox("Selected value: " + selected, MsgBox.MF_INFORMATION + MsgBox.MF_OK_BUTTON);
        }
    }

    private TDialog loadSampleDialog() {
        try (InputStream is = DemoApp.class.getResourceAsStream(SAMPLE_DIALOG_FILE)) {
            if (is == null) {
                return null;
            }
            TStream ts = new TStream(is);
            return (TDialog) ts.loadView();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
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
                        TMenuBar.newItem("~F~ile dialog", null, KeyCode.KB_NO_KEY, CM_DLG_FILE, HelpContext.HC_NO_CONTEXT,
                        TMenuBar.newItem("~R~adio buttons", null, KeyCode.KB_NO_KEY, CM_DLG_RADIO_BUTTONS, HelpContext.HC_NO_CONTEXT,
                        TMenuBar.newItem("~C~heck boxes", null, KeyCode.KB_NO_KEY, CM_DLG_CHECK_BOXES, HelpContext.HC_NO_CONTEXT,
                        null)))))),
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
                case CM_DLG_RADIO_BUTTONS:
                    doDlgRadioButtons();
                    break;
                case CM_DLG_CHECK_BOXES:
                    doDlgCheckBoxes();
                    break;
                case CM_DLG_FILE:
                    try (InputStream is = DemoApp.class.getResourceAsStream(SAMPLE_DIALOG_FILE)) {
                        if (is == null) {
                            MsgBox.messageBox("sampleDialog.bin not found", MsgBox.MF_ERROR + MsgBox.MF_OK_BUTTON);
                            break;
                        }
                        TStream ts = new TStream(is);
                        TDialog dialog = (TDialog) ts.loadView();
                        desktop.execView(dialog);
                    } catch (IOException e) {
                        MsgBox.messageBox("Error loading dialog", MsgBox.MF_ERROR + MsgBox.MF_OK_BUTTON);
                    }
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

