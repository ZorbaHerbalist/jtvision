package info.qbnet.jtvision.backend;

import info.qbnet.jtvision.backend.factory.SwingFactory;
import info.qbnet.jtvision.core.Screen;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * A Swing-based implementation of the rendering backend.
 */
public class SwingBasicBackend extends JPanel implements SwingFactory.SwingBackendWithPanel {

    private static final int CHAR_WIDTH = 9;
    private static final int CHAR_HEIGHT = 16;
    private final Screen buffer;
    private final BufferedImage backBuffer;

    /**
     * Constructs a Swing rendering panel for the given screen.
     * @param buffer the screen buffer to render
     */
    public SwingBasicBackend(Screen buffer) {
        this.buffer = buffer;
        int width = buffer.getWidth() * CHAR_WIDTH;
        int height = buffer.getHeight() * CHAR_HEIGHT;
        this.backBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        setPreferredSize(new Dimension(width, height));
        setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
    }

    @Override
    public void render() {
        drawToBackBuffer();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(backBuffer, 0, 0, null);
    }

    private void drawToBackBuffer() {
        Graphics2D g2d = backBuffer.createGraphics();
        g2d.setFont(getFont());
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        for (int y = 0; y < buffer.getHeight(); y++) {
            for (int x = 0; x < buffer.getWidth(); x++) {
                drawChar(g2d, x, y, buffer.getChar(x, y));
            }
        }
        g2d.dispose();
    }

    /**
     * Draws a single character cell at the given coordinates.
     * @param g the graphics context
     * @param x column position
     * @param y row position
     * @param sc the screen character to draw
     */
    private void drawChar(Graphics g, int x, int y, Screen.ScreenChar sc) {
        g.setColor(sc.getBackground());
        g.fillRect(x * CHAR_WIDTH, y * CHAR_HEIGHT, CHAR_WIDTH, CHAR_HEIGHT);
        g.setColor(sc.getForeground());
        g.drawString(Character.toString(sc.getCharacter()), x * CHAR_WIDTH, (y + 1) * CHAR_HEIGHT - 4);
    }

    @Override
    public JPanel getPanel() {
        return this;
    }
}

