package info.qbnet.jtvision;

import info.qbnet.jtvision.backend.Backend;
import info.qbnet.jtvision.util.Screen;
import info.qbnet.jtvision.util.DosPalette;

import java.awt.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Console class provides a text-mode interface for writing to a screen buffer.
 */

public class Console {

    private final Screen screen;
    private final Backend backend;
    private final ScheduledExecutorService scheduler;
    private volatile boolean isScreenDirty = false;

    /**
     * Constructs a Console with the given screen buffer and rendering backend.
     * @param screen the screen buffer
     * @param backend the rendering backend
     */
    public Console(Screen screen, Backend backend) {
        this(screen, backend, 33);
    }

    public Console(Screen screen, Backend backend, long refreshIntervalMs) {
        this.screen = screen;
        this.backend = backend;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.scheduler.scheduleAtFixedRate(this::flush, refreshIntervalMs, refreshIntervalMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Prints a string at the specified coordinates with specified colors.
     * @param x horizontal position
     * @param y vertical position
     * @param text the text to print
     * @param foreground foreground color
     * @param background background color
     */
    public void putString(int x, int y, String text, Color foreground, Color background) {
        if (text == null) {
            System.err.println("putString(): text is null. Ignored.");
            return;
        }
        if (!screen.isValidColor(foreground, background)) {
            System.err.printf("putString(): null color argument at (%d,%d). Ignored.%n", x, y);
            return;
        }
        if (!screen.isInBounds(x, y)) {
            System.err.printf("putString(): coordinates out of bounds (%d,%d). Ignored.%n", x, y);
            return;
        }

        int maxLength = screen.getWidth() - x;
        int len = Math.min(text.length(), maxLength);
        for (int i = 0; i < len; i++) {
            screen.setChar(x + i, y, text.charAt(i), foreground, background);
        }
        isScreenDirty = true;
    }

    /**
     * Prints a string using a Turbo Vision-style color attribute byte.
     *
     * @param x         horizontal position
     * @param y         vertical position
     * @param text      the text to print
     * @param attribute color attribute encoded as in DOS Turbo Vision
     */
    public void putString(int x, int y, String text, int attribute) {
        putString(x, y, text, DosPalette.getForeground(attribute), DosPalette.getBackground(attribute));
    }

    /**
     * Clears the screen buffer and renders the empty screen.
     */
    public void clearScreen() {
        screen.clear();
        isScreenDirty = true;
    }

    /**
     * Renders pending changes if the screen buffer is marked dirty.
     */
    public void flush() {
        if (isScreenDirty) {
            backend.renderScreen();
            isScreenDirty = false;
        }
    }

    /**
     * Stops the internal flush scheduler.
     */
    public void shutdown() {
        scheduler.shutdown();
    }

}
