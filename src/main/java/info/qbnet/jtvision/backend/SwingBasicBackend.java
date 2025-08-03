package info.qbnet.jtvision.backend;

import info.qbnet.jtvision.util.Screen;
import info.qbnet.jtvision.util.buffer.CharacterBuffer.CharacterCell;

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
    protected void drawGlyph(Graphics2D g, int x, int y, CharacterCell sc) {
        g.setColor(sc.background());
        g.fillRect(x * getCellWidth(), y * getCellHeight(), getCellWidth(), getCellHeight());
        g.setColor(sc.foreground());
        g.drawString(Character.toString(sc.character()), x * getCellWidth(), (y + 1) * getCellHeight() - 4);
    }

}

