package info.qbnet.jtvision.backend;

import info.qbnet.jtvision.core.Screen;

import java.awt.*;

/**
 * A Swing-based implementation of the rendering backend.
 */
public class SwingBasicBackend extends AbstractSwingBackend {

    private static final Integer CHAR_WIDTH = 8;
    private static final Integer CHAR_HEIGHT = 16;

    /**
     * Constructs a Swing rendering panel for the given screen.
     * @param buffer the screen buffer to render
     */
    public SwingBasicBackend(Screen buffer) {
        super(buffer, CHAR_WIDTH, CHAR_HEIGHT);
        setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(backBuffer, 0, 0, null);
    }

    @Override
    protected void configureGraphics(Graphics2D g2d) {
        g2d.setFont(getFont());
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
    }

    @Override
    protected void drawChar(Graphics2D g, int x, int y, Screen.ScreenChar sc) {
        g.setColor(sc.getBackground());
        g.fillRect(x * CHAR_WIDTH, y * CHAR_HEIGHT, CHAR_WIDTH, CHAR_HEIGHT);
        g.setColor(sc.getForeground());
        g.drawString(Character.toString(sc.getCharacter()), x * CHAR_WIDTH, (y + 1) * CHAR_HEIGHT - 4);
    }

}

