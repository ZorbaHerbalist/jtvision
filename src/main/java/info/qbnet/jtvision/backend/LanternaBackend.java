package info.qbnet.jtvision.backend;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import info.qbnet.jtvision.backend.factory.GuiComponent;
import info.qbnet.jtvision.util.IBuffer.CharacterCell;

import java.io.IOException;

/**
 * Backend implementation using the Lanterna library to render the console.
 */
public class LanternaBackend implements GuiComponent<Screen> {

    private final info.qbnet.jtvision.util.Screen screenBuffer;
    private final Integer cellWidth;
    private final Integer cellHeight;
    private Screen terminalScreen;

    public LanternaBackend(info.qbnet.jtvision.util.Screen screen,
                           int cellWidth,
                           int cellHeight) {
        this.screenBuffer = screen;
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
    }

    @Override
    public void afterInitialization() {
        try {
            DefaultTerminalFactory factory = new DefaultTerminalFactory();
            factory.setInitialTerminalSize(new TerminalSize(screenBuffer.getWidth(),
                    screenBuffer.getHeight()));
            terminalScreen = factory.createScreen();
            terminalScreen.startScreen();
            terminalScreen.setCursorPosition(null);
        } catch (IOException e) {
            throw new RuntimeException("Failed to start Lanterna screen", e);
        }
    }

    @Override
    public void renderScreen() {
        TextGraphics tg = terminalScreen.newTextGraphics();
        for (int y = 0; y < screenBuffer.getHeight(); y++) {
            for (int x = 0; x < screenBuffer.getWidth(); x++) {
                CharacterCell ch = screenBuffer.getChar(x, y);
                tg.setForegroundColor(toLanterna(ch.foreground()));
                tg.setBackgroundColor(toLanterna(ch.background()));
                tg.putString(x, y, String.valueOf(ch.character()));
            }
        }
        try {
            terminalScreen.refresh();
        } catch (IOException e) {
            throw new RuntimeException("Failed to refresh Lanterna screen", e);
        }
    }

    private static TextColor toLanterna(java.awt.Color c) {
        return new TextColor.RGB(c.getRed(), c.getGreen(), c.getBlue());
    }

    @Override
    public Integer getCellWidth() {
        return cellWidth;
    }

    @Override
    public Integer getCellHeight() {
        return cellHeight;
    }

    @Override
    public Screen getUIComponent() {
        return terminalScreen;
    }

    /** Stops the Lanterna screen. */
    public void stop() {
        try {
            if (terminalScreen != null) {
                terminalScreen.stopScreen();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to stop Lanterna screen", e);
        }
    }
}

