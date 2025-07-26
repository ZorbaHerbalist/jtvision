package info.qbnet.jtvision.backend;

import info.qbnet.jtvision.backend.factory.GuiComponent;
import info.qbnet.jtvision.core.Screen;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Base class for Swing based backends implementing the common buffering
 * logic. Subclasses only need to supply font specific setup and character
 * rendering.
 */
public abstract class AbstractSwingBackend extends JPanel
        implements GuiComponent<JPanel>, CharDimensions {

    protected final Screen buffer;
    protected final BufferedImage backBuffer;
    private final int charWidth;
    private final int charHeight;

    protected AbstractSwingBackend(Screen buffer, int charWidth, int charHeight) {
        this.buffer = buffer;
        this.charWidth = charWidth;
        this.charHeight = charHeight;
        int width = buffer.getWidth() * charWidth;
        int height = buffer.getHeight() * charHeight;
        this.backBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        setPreferredSize(new Dimension(width, height));
    }

    @Override
    public void render() {
        drawToBackBuffer();
        repaint();
    }

    protected void drawToBackBuffer() {
        Graphics2D g2d = backBuffer.createGraphics();
        configureGraphics(g2d);
        for (int y = 0; y < buffer.getHeight(); y++) {
            for (int x = 0; x < buffer.getWidth(); x++) {
                drawChar(g2d, x, y, buffer.getChar(x, y));
            }
        }
        g2d.dispose();
    }

    /**
     * Hook for subclasses to configure the graphics context before drawing.
     * The default implementation does nothing.
     */
    protected void configureGraphics(Graphics2D g2d) {
        // no-op
    }

    protected abstract void drawChar(Graphics2D g, int x, int y, Screen.ScreenChar sc);

    @Override
    public int getCharWidth() {
        return charWidth;
    }

    @Override
    public int getCharHeight() {
        return charHeight;
    }

    @Override
    public JPanel getNativeComponent() {
        return this;
    }
}
