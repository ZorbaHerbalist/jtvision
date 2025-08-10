package info.qbnet.jtvision.backend;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import info.qbnet.jtvision.backend.factory.GuiComponent;
import info.qbnet.jtvision.util.DosPalette;

import java.io.IOException;
import java.util.Optional;

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
                short cell = screenBuffer.getCell(x, y);
                char ch = (char) (cell & 0xFF);
                int attr = (cell >>> 8) & 0xFF;
                java.awt.Color fg = DosPalette.getForeground(attr);
                java.awt.Color bg = DosPalette.getBackground(attr);
                tg.setForegroundColor(toLanterna(fg));
                tg.setBackgroundColor(toLanterna(bg));
                tg.putString(x, y, String.valueOf(ch));
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

    @Override
    public Optional<info.qbnet.jtvision.core.event.KeyEvent> pollKeyEvent() {
        try {
            if (terminalScreen == null) {
                return Optional.empty();
            }
            KeyStroke ks = terminalScreen.pollInput();
            if (ks == null) {
                return Optional.empty();
            }
            if (ks.getCharacter() != null) {
                return Optional.of(new info.qbnet.jtvision.core.event.KeyEvent(ks.getCharacter(), ks.getKeyType().ordinal()));
            }
            return Optional.of(new info.qbnet.jtvision.core.event.KeyEvent(ks.getKeyType().ordinal()));
        } catch (IOException e) {
            return Optional.empty();
        }
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

