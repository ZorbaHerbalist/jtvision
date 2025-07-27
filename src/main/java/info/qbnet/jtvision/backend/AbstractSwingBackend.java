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
        implements GuiComponent<JPanel> {

    protected final Screen screen;
    protected final BufferedImage backBuffer;
    private final Integer charWidth;
    private final Integer charHeight;

    protected AbstractSwingBackend(Screen screen, Integer charWidth, Integer charHeight) {
        this.screen = screen;
        this.charWidth = charWidth;
        this.charHeight = charHeight;
        int width = screen.getWidth() * charWidth;
        int height = screen.getHeight() * charHeight;
        this.backBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        setPreferredSize(new Dimension(width, height));
    }

    @Override
    public void afterInitialization() {
        // default implementation does nothing
    }

    @Override
    public void renderScreen() {
        drawToBackBuffer();
        repaint();
    }

    protected void drawToBackBuffer() {
        Graphics2D g2d = backBuffer.createGraphics();
        configureGraphics(g2d);
        for (int y = 0; y < screen.getHeight(); y++) {
            for (int x = 0; x < screen.getWidth(); x++) {
                drawGlyph(g2d, x, y, screen.getChar(x, y));
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

    protected abstract void drawGlyph(Graphics2D g, int x, int y, Screen.ScreenChar sc);

    @Override
    public Integer getCellWidth() {
        return charWidth;
    }

    @Override
    public Integer getCellHeight() {
        return charHeight;
    }

    @Override
    public JPanel getNativeComponent() {
        return this;
    }
}
