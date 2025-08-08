package info.qbnet.jtvision.backend;

import info.qbnet.jtvision.backend.factory.GuiComponent;
import info.qbnet.jtvision.util.Screen;
import info.qbnet.jtvision.util.DosPalette;

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
    private final Integer cellWidth;
    private final Integer cellHeight;

    protected AbstractSwingBackend(Screen screen, Integer cellWidth, Integer cellHeight) {
        this.screen = screen;
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        int width = screen.getWidth() * cellWidth;
        int height = screen.getHeight() * cellHeight;
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
                short cell = screen.getCell(x, y);
                char ch = (char) (cell & 0xFF);
                int attr = (cell >>> 8) & 0xFF;
                java.awt.Color fg = DosPalette.getForeground(attr);
                java.awt.Color bg = DosPalette.getBackground(attr);
                drawGlyph(g2d, x, y, ch, fg, bg);
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

    protected abstract void drawGlyph(Graphics2D g, int x, int y, char ch,
                                      java.awt.Color fg, java.awt.Color bg);

    @Override
    public Integer getCellWidth() {
        return cellWidth;
    }

    @Override
    public Integer getCellHeight() {
        return cellHeight;
    }

    @Override
    public JPanel getUIComponent() {
        return this;
    }
}
