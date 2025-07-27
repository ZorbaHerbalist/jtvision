package info.qbnet.jtvision.backend;

import info.qbnet.jtvision.core.Screen;

import java.awt.*;

/**
 * A Swing-based implementation of the rendering backend.
 */
public class SwingBasicBackend extends AbstractSwingBackend {

    /**
     * Constructs a Swing rendering panel for the given screen.
     *
     * @param screen the screen buffer to render
     * @param charWidth width of a character cell in pixels
     * @param charHeight height of a character cell in pixels
     */
    public SwingBasicBackend(Screen screen, int charWidth, int charHeight) {
        super(screen, charWidth, charHeight);
        this.afterInitialization();
    }

    @Override
    public void afterInitialization() {
        initResources();
    }

    /** Load Swing resources such as fonts. */
    protected void initResources() {
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
    protected void drawGlyph(Graphics2D g, int x, int y, Screen.ScreenChar sc) {
        g.setColor(sc.getBackground());
        g.fillRect(x * getCellWidth(), y * getCellHeight(), getCellWidth(), getCellHeight());
        g.setColor(sc.getForeground());
        g.drawString(Character.toString(sc.getCharacter()), x * getCellWidth(), (y + 1) * getCellHeight() - 4);
    }

}

