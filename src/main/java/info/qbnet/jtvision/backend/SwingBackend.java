package info.qbnet.jtvision.backend;

import javax.swing.*;
import java.awt.*;

/**
 * A Swing-based implementation of the rendering backend.
 */
public class SwingBackend extends JPanel implements Backend {

    private static final int CHAR_WIDTH = 9;
    private static final int CHAR_HEIGHT = 16;
    private Screen buffer;

    /**
     * Constructs a Swing rendering panel for the given screen.
     * @param buffer the screen buffer to render
     */
    public SwingBackend(Screen buffer) {
        this.buffer = buffer;
        Dimension size = new Dimension(buffer.getCols() * CHAR_WIDTH,
                buffer.getRows() * CHAR_HEIGHT);
        setPreferredSize(size);
        setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
    }

    @Override
    public void render() {
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (int y = 0; y < buffer.getRows(); y++) {
            for (int x = 0; x < buffer.getCols(); x++) {
                drawChar(g, x, y, buffer.getChar(x, y));
            }
        }
    }

    /**
     * Draws a single character cell at the given coordinates.
     * @param g the graphics context
     * @param x column position
     * @param y row position
     * @param sc the screen character to draw
     */
    private void drawChar(Graphics g, int x, int y, Screen.ScreenChar sc) {
        g.setColor(sc.bg);
        g.fillRect(x * CHAR_WIDTH, y * CHAR_HEIGHT, CHAR_WIDTH, CHAR_HEIGHT);
        g.setColor(sc.fg);
        g.drawString(Character.toString(sc.c), x * CHAR_WIDTH, (y + 1) * CHAR_HEIGHT - 4);
    }

}
