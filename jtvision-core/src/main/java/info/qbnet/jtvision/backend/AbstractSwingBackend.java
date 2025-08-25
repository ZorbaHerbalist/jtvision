package info.qbnet.jtvision.backend;

import info.qbnet.jtvision.backend.factory.GuiComponent;
import info.qbnet.jtvision.util.Screen;
import info.qbnet.jtvision.util.DosPalette;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import info.qbnet.jtvision.core.event.KeyCodeMapper;
import info.qbnet.jtvision.core.event.TEvent;
import info.qbnet.jtvision.core.objects.TPoint;
import java.awt.image.BufferedImage;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

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
    private final Queue<TEvent> events = new ConcurrentLinkedQueue<>();
    private volatile int mouseButtons = 0;
    private volatile int mouseX = 0;
    private volatile int mouseY = 0;
    private volatile byte shiftState = 0;

    protected AbstractSwingBackend(Screen screen, Integer cellWidth, Integer cellHeight) {
        this.screen = screen;
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        int width = screen.getWidth() * cellWidth;
        int height = screen.getHeight() * cellHeight;
        this.backBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        setPreferredSize(new Dimension(width, height));

        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                updateShiftState(e, true);
                int code = mapKeyCode(e.getKeyCode());
                boolean shift = e.isShiftDown();
                boolean ctrl = e.isControlDown();
                boolean alt = e.isAltDown();
                int withMods = KeyCodeMapper.applyModifiers(code, shift, ctrl, alt);
                char ch = KeyCodeMapper.toChar(code, shift);
                int scan = code;
                TEvent ev = new TEvent();
                ev.what = TEvent.EV_KEYDOWN;
                ev.key.keyCode = withMods;
                ev.key.charCode = ch;
                ev.key.scanCode = (byte) scan;
                events.add(ev);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                updateShiftState(e, false);
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                updateMousePosition(e);
                if (e.getButton() == MouseEvent.BUTTON1) {
                    mouseButtons |= 1;
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    mouseButtons |= 2;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                updateMousePosition(e);
                if (e.getButton() == MouseEvent.BUTTON1) {
                    mouseButtons &= ~1;
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    mouseButtons &= ~2;
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                updateMousePosition(e);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                updateMousePosition(e);
            }
        });
    }

    private void updateMousePosition(MouseEvent e) {
        int x = e.getX() / cellWidth;
        int y = e.getY() / cellHeight;
        x = Math.max(0, Math.min(screen.getWidth() - 1, x));
        y = Math.max(0, Math.min(screen.getHeight() - 1, y));
        mouseX = x;
        mouseY = y;
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

    private void updateShiftState(KeyEvent e, boolean pressed) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_SHIFT -> {
                if (e.getKeyLocation() == KeyEvent.KEY_LOCATION_RIGHT) {
                    if (pressed) {
                        shiftState |= 0x01;
                    } else {
                        shiftState &= ~0x01;
                    }
                } else {
                    if (pressed) {
                        shiftState |= 0x02;
                    } else {
                        shiftState &= ~0x02;
                    }
                }
            }
            case KeyEvent.VK_CONTROL -> {
                if (pressed) {
                    shiftState |= 0x04;
                } else {
                    shiftState &= ~0x04;
                }
            }
            case KeyEvent.VK_ALT -> {
                if (pressed) {
                    shiftState |= 0x08;
                } else {
                    shiftState &= ~0x08;
                }
            }
        }
    }

    @Override
    public byte getShiftState() {
        return shiftState;
    }

    /**
     * Maps the Swing key code to the unified scheme. Swing already uses the
     * standard AWT codes so the value is returned unchanged.
     */
    public static int mapKeyCode(int keyCode) {
        return keyCode;
    }

    @Override
    public Optional<TEvent> pollEvent() {
        return Optional.ofNullable(events.poll());
    }

    @Override
    public int getMouseButtons() {
        return mouseButtons;
    }

    @Override
    public TPoint getMouseLocation() {
        return new TPoint(mouseX, mouseY);
    }
}
