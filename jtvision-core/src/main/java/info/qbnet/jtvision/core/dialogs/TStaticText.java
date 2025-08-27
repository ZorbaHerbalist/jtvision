package info.qbnet.jtvision.core.dialogs;

import info.qbnet.jtvision.core.objects.TRect;
import info.qbnet.jtvision.core.views.TDrawBuffer;
import info.qbnet.jtvision.core.views.TPalette;
import info.qbnet.jtvision.core.views.TView;

/**
 * A non-interactive text view used in dialogs for labels or explanatory text.
 */
public class TStaticText extends TView {

    protected String text;

    public static final TPalette C_STATIC_TEXT = new TPalette(TPalette.parseHexString("\\x06"));

    public TStaticText(TRect bounds, final String text) {
        super(bounds);
        this.text = text;
    }

    @Override
    public void draw() {
        logger.trace("{} TStaticText@draw()", getLogName());

        TDrawBuffer buf = new TDrawBuffer();
        short color = getColor((short) 1);
        String s = getText();
        int L = s.length();
        int P = 0; // current position in string
        int Y = 0; // current row
        boolean center = false;

        while (Y < size.y) {
            // start with a cleared line
            buf.moveChar(0, ' ', color, size.x);

            if (P < L) {
                // optional centering marker
                if (s.charAt(P) == 3) { // ETX
                    center = true;
                    P++;
                }

                int I = P; // start of segment
                int J;

                // find break position
                do {
                    J = P;
                    while (P < L && s.charAt(P) == ' ') P++;
                    while (P < L && s.charAt(P) != ' ' && s.charAt(P) != '\r' && s.charAt(P) != '\n') P++;
                } while (!(P >= L || P >= I + size.x || s.charAt(P) == '\r' || s.charAt(P) == '\n'));

                // adjust if word would overflow line
                if (P > I + size.x) {
                    if (J > I) {
                        P = J;
                    } else {
                        P = I + size.x;
                    }
                }

                // compute starting column when centered
                if (center) {
                    J = (size.x - (P - I)) / 2;
                } else {
                    J = 0;
                }

                // copy substring into buffer
                if (P > I) {
                    String seg = s.substring(I, P);
                    buf.moveStr(J, seg, color);
                }

                // skip trailing spaces
                while (P < L && s.charAt(P) == ' ') P++;

                // handle line breaks (CR, LF or CRLF)
                if (P < L && (s.charAt(P) == '\r' || s.charAt(P) == '\n')) {
                    center = false;
                    if (s.charAt(P) == '\r' && P + 1 < L && s.charAt(P + 1) == '\n') {
                        P += 2;
                    } else {
                        P++;
                    }
                }
            }

            writeLine(0, Y, size.x, 1, buf.buffer);
            Y++;
        }
    }

    @Override
    public TPalette getPalette() {
        return C_STATIC_TEXT;
    }

    public String getText() {
        return text != null ? text : "";
    }

}
