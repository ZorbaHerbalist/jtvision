package info.qbnet.jtvision.core.dialogs;

import info.qbnet.jtvision.core.constants.Command;
import info.qbnet.jtvision.core.event.TEvent;
import info.qbnet.jtvision.core.objects.TPoint;
import info.qbnet.jtvision.core.objects.TRect;
import info.qbnet.jtvision.core.views.TDrawBuffer;
import info.qbnet.jtvision.core.views.TGroup;
import info.qbnet.jtvision.core.views.TPalette;
import info.qbnet.jtvision.core.views.TView;
import info.qbnet.jtvision.core.objects.TStream;
import java.io.IOException;

import static info.qbnet.jtvision.core.constants.KeyCode.getAltCode;

public class TButton extends TView {

    public static final int CLASS_ID = 5;

    static {
        TStream.registerType(CLASS_ID, TButton::new);
    }

    @Override
    public int getClassId() {
        return CLASS_ID;
    }

    public static final int BF_NORMAL    = 0x00;
    public static final int BF_DEFAULT   = 0x01;
    public static final int BF_LEFT_JUST = 0x02;
    public static final int BF_BROADCAST = 0x04;
    public static final int BF_GRAB_FOCUS= 0x08;

    public static final TPalette C_BUTTON = new TPalette(TPalette.parseHexString("\\x0A\\x0B\\x0C\\x0D\\x0E\\x0E\\x0E\\x0F"));

    protected String title;
    protected int command;
    protected int flags;
    protected boolean amDefault;

    public TButton(TRect bounds, String title, int command, int flags) {
        super(bounds);
        this.options |= Options.OF_SELECTABLE | Options.OF_FIRST_CLICK |
                Options.OF_PRE_PROCESS | Options.OF_POST_PROCESS;
        this.eventMask |= TEvent.EV_BROADCAST;
        this.title = title;
        this.command = command;
        this.flags = flags;
        this.amDefault = (flags & BF_DEFAULT) != 0;
        if (!commandEnabled(command)) {
            state |= State.SF_DISABLED;
        }
    }

    public TButton(TStream stream) {
        super(stream);
        try {
            title = stream.readString();
            command = stream.readInt();
            flags = stream.readInt();
            amDefault = stream.readInt() != 0;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        setState(State.SF_DISABLED, !commandEnabled(command));
    }

    @Override
    public void draw() {
        drawState(false);
    }

    /**
     * Renders the button either in pressed (down=true) or normal state.
     */
    protected void drawState(boolean down) {
        logger.trace("{} TButton@drawState(down={})", getLogName(), down);

        TDrawBuffer buf = new TDrawBuffer();
        short cShadow = getColor((short)8);
        short cButton;
        int s = size.x - 1;
        int t = size.y / 2 - 1;

        if ((state & State.SF_DISABLED) != 0) {
            cButton = getColor((short)0x0404);
        } else {
            cButton = getColor((short)0x0501);
            if ((state & State.SF_ACTIVE) != 0) {
                if ((state & State.SF_SELECTED) != 0) {
                    cButton = getColor((short)0x0703);
                } else if (amDefault) {
                    cButton = getColor((short)0x0602);
                }
            }
        }

        char ch = ' ';
        for (int y = 0; y < size.y - 1; y++) {
            buf.moveChar(0, ' ', cButton & 0xFF, size.x);
            // left edge highlight
            buf.buffer[0] = (short) ((cShadow << 8) | (buf.buffer[0] & 0xFF));

            int i;
            if (down) {
                buf.buffer[1] = (short) ((cShadow << 8) | (buf.buffer[1] & 0xFF));
                ch = ' ';
                i = 2;
            } else {
                buf.buffer[s] = (short) ((cShadow << 8) | (buf.buffer[s] & 0xFF));
                if (showMarkers) {
                    ch = ' ';
                } else {
                    if (y == 0) {
                        buf.buffer[s] = (short) ((buf.buffer[s] & 0xFF00) | 0xDC); // lower half block
                    } else {
                        buf.buffer[s] = (short) ((buf.buffer[s] & 0xFF00) | 0xDB); // full block
                    }
                    ch = (char) 0xDF; // upper half block
                }
                i = 1;
            }

            if (y == t && title != null) {
                int L;
                if ((flags & BF_LEFT_JUST) != 0) {
                    L = 1;
                } else {
                    L = (s - titleLength() - 1) / 2;
                    if (L < 1) {
                        L = 1;
                    }
                }
                buf.moveCStr(i + L, title, cButton);
                if (showMarkers && !down) {
                    int scOff;
                    if ((state & State.SF_SELECTED) != 0) {
                        scOff = 0;
                    } else if (amDefault) {
                        scOff = 2;
                    } else {
                        scOff = 4;
                    }
                    buf.buffer[0] = (short) ((buf.buffer[0] & 0xFF00) | SPECIAL_CHARS[scOff]);
                    buf.buffer[s] = (short) ((buf.buffer[s] & 0xFF00) | SPECIAL_CHARS[scOff + 1]);
                }
            }

            if (showMarkers && !down) {
                buf.buffer[1] = (short) ((buf.buffer[1] & 0xFF00) | '[');
                buf.buffer[s - 1] = (short) ((buf.buffer[s - 1] & 0xFF00) | ']');
            }

            writeLine(0, y, size.x, 1, buf.buffer);
        }

        buf.moveChar(0, ' ', cShadow & 0xFF, 2);
        buf.moveChar(2, ch, cShadow & 0xFF, s - 1);
        writeLine(0, size.y - 1, size.x, 1, buf.buffer);
    }

    @Override
    public void store(TStream stream) {
        super.store(stream);
        try {
            stream.writeString(title);
            stream.writeInt(command);
            stream.writeInt(flags);
            stream.writeInt(amDefault ? 1 : 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public TPalette getPalette() {
        return C_BUTTON;
    }

    private int titleLength() {
        if (title == null) {
            return 0;
        }
        int count = 0;
        boolean tilde = false;
        for (int i = 0; i < title.length(); i++) {
            char c = title.charAt(i);
            if (c == '~') {
                tilde = !tilde;
                continue;
            }
            count++;
        }
        return count;
    }

    private static char hotKey(String s) {
        if (s == null) {
            return 0;
        }
        boolean tilde = false;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch == '~') {
                tilde = !tilde;
            } else if (tilde) {
                return Character.toUpperCase(ch);
            }
        }
        return 0;
    }

    @Override
    public void handleEvent(TEvent event) {
        TRect clickRect = new TRect();
        getExtent(clickRect);
        clickRect.a.x++;
        clickRect.b.x--;
        clickRect.b.y--;

        if (event.what == TEvent.EV_MOUSE_DOWN) {
            TPoint mouse = new TPoint();
            makeLocal(event.mouse.where, mouse);
            if (!clickRect.contains(mouse)) {
                clearEvent(event);
            }
        }

        if ((flags & BF_GRAB_FOCUS) != 0) {
            super.handleEvent(event);
        }

        switch (event.what) {
            case TEvent.EV_MOUSE_DOWN:
                if ((state & State.SF_DISABLED) == 0) {
                    clickRect.b.x++;
                    boolean down = false;
                    TPoint mouse = new TPoint();
                    do {
                        makeLocal(event.mouse.where, mouse);
                        boolean inside = clickRect.contains(mouse);
                        if (down != inside) {
                            down = !down;
                            drawState(down);
                        }
                    } while (mouseEvent(event, TEvent.EV_MOUSE_MOVE));
                    if (down) {
                        press();
                        drawState(false);
                    }
                }
                clearEvent(event);
                break;
            case TEvent.EV_KEYDOWN:
                char c = hotKey(title);
                boolean doPress = false;
                if (c != 0) {

                    if (event.key.keyCode == getAltCode(c)) {
                        doPress = true;
                    } else if (owner != null && owner.phase == TGroup.Phase.POST_PROCESS &&
                            Character.toUpperCase(event.key.charCode) == c) {
                        doPress = true;
                    }
                }
                if (!doPress && (state & State.SF_FOCUSED) != 0 && event.key.charCode == ' ') {
                    doPress = true;
                }
                if (doPress) {
                    press();
                    clearEvent(event);
                }
                break;
            case TEvent.EV_BROADCAST:
                switch (event.msg.command) {
                    case Command.CM_DEFAULT:
                        if (amDefault) {
                            press();
                            clearEvent(event);
                        }
                        break;
                    case Command.CM_GRAB_DEFAULT:
                    case Command.CM_RELEASE_DEFAULT:
                        if ((flags & BF_DEFAULT) != 0) {
                            amDefault = event.msg.command == Command.CM_RELEASE_DEFAULT;
                            drawView();
                        }
                        break;
                    case Command.CM_COMMAND_SET_CHANGED:
                        setState(State.SF_DISABLED, !commandEnabled(command));
                        drawView();
                        break;
                }
                break;
        }
    }

    /**
     * Toggles the button's default state.
     * <p>
     * Translated from Turbo Pascal's {@code TButton.MakeDefault} in
     * {@code DIALOGS.PAS}.
     * </p>
     *
     * @param enable {@code true} to make this button the default,
     *               {@code false} to release default status
     */
    public void makeDefault(boolean enable) {
        if ((flags & BF_DEFAULT) == 0) {
            int c = enable ? Command.CM_GRAB_DEFAULT : Command.CM_RELEASE_DEFAULT;
            message(owner, TEvent.EV_BROADCAST, c, this);
            amDefault = enable;
            drawView();
        }
    }

    public void press() {
        message(owner, TEvent.EV_BROADCAST, Command.CM_RECORD_HISTORY, null);
        if ((flags & BF_BROADCAST) != 0) {
            message(owner, TEvent.EV_BROADCAST, command, this);
        } else {
            TEvent e = new TEvent();
            e.what = TEvent.EV_COMMAND;
            e.msg.command = command;
            e.msg.infoPtr = this;
            putEvent(e);
        }
    }

    @Override
    public void setState(int state, boolean enable) {
        super.setState(state, enable);
        if ((state & (State.SF_SELECTED | State.SF_ACTIVE)) != 0) {
            drawView();
        }
        if ((state & State.SF_FOCUSED) != 0) {
            makeDefault(enable);
        }
    }

}
