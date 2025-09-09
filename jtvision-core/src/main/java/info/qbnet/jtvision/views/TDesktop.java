package info.qbnet.jtvision.views;

import info.qbnet.jtvision.util.Command;
import info.qbnet.jtvision.event.TEvent;
import info.qbnet.jtvision.util.TPoint;
import info.qbnet.jtvision.util.TRect;

import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class TDesktop extends TGroup {

    private TBackground background;
    private boolean tileColumnsFirst = false;

    public TDesktop(TRect bounds) {
        super(bounds);
        setGrowModes(EnumSet.of(GrowMode.GF_GROW_HI_X, GrowMode.GF_GROW_HI_Y));

        logger.debug("{} TDesktop@TDesktop(bounds={})", getLogName(), bounds);

        initBackground();
        if (background != null) {
            insert(background);
        }
    }

    private boolean tileable(TView view) {
        return (view.options & Options.OF_TILEABLE) != 0 && (view.state & State.SF_VISIBLE) != 0;
    }

    /**
     * Arranges tileable views in a cascading layout within the given rectangle.
     * <p>
     * Each tileable, visible view is offset by one row and column from the previous, producing the
     * classic cascade effect.
     * </p>
     *
     * @param r the rectangle in which to cascade the views
     */
    public void cascade(TRect r) {
        logger.trace("{} TDesktop@cascade(r={})", getLogName(), r);

        AtomicInteger cascadeNum = new AtomicInteger(0);
        AtomicReference<TView> lastView = new AtomicReference<>();
        forEach(v -> {
            if (tileable(v)) {
                cascadeNum.incrementAndGet();
                lastView.set(v);
            }
        });

        if (cascadeNum.get() > 0) {
            TPoint min = new TPoint();
            TPoint max = new TPoint();
            lastView.get().sizeLimits(min, max);

            int widthAvail = r.b.x - r.a.x - cascadeNum.get();
            int heightAvail = r.b.y - r.a.y - cascadeNum.get();

            if (min.x > widthAvail || min.y > heightAvail) {
                tileError();
            } else {
                AtomicInteger num = new AtomicInteger(cascadeNum.get() - 1);
                lock();
                forEach(view -> {
                    if (tileable(view) && num.get() >= 0) {
                        TRect nr = new TRect();
                        nr.copy(r);
                        int offset = num.get();
                        nr.a.x += offset;
                        nr.a.y += offset;
                        view.locate(nr);
                        num.decrementAndGet();
                    }
                });
                unlock();
            }
        }
    }

    @Override
    public void handleEvent(TEvent event) {
        super.handleEvent(event);
        if (event.what == TEvent.EV_COMMAND) {
            switch (event.msg.command) {
                case Command.CM_NEXT:
                    focusNext(false);
                    break;
                case Command.CM_PREV:
                    if (valid(Command.CM_RELEASED_FOCUS)) {
                        current.putInFrontOf(background);
                    }
                    break;
                default:
                    return;
            }
            clearEvent(event);
        }
    }

    public void initBackground() {
        logger.trace("{} TDesktop@initBackground()", getLogName());

        TRect rect = new TRect();
        getExtent(rect);
        background = new TBackground(rect, (char) 176);
    }

    private static int dividerLoc(int lo, int hi, int num, int pos) {
        return (int) (((long) (hi - lo) * pos) / num) + lo;
    }

    private static TRect calcTileRect(int pos, TRect r, int numCols, int numRows, int leftOver) {
        int x, y, d;
        d = (numCols - leftOver) * numRows;
        if (pos < d) {
            x = pos / numRows;
            y = pos % numRows;
        } else {
            x = (pos - d) / (numRows + 1) + (numCols - leftOver);
            y = (pos - d) % (numRows + 1);
        }
        TRect nr = new TRect();
        nr.a.x = dividerLoc(r.a.x, r.b.x, numCols, x);
        nr.b.x = dividerLoc(r.a.x, r.b.x, numCols, x + 1);
        if (pos >= d) {
            nr.a.y = dividerLoc(r.a.y, r.b.y, numRows + 1, y);
            nr.b.y = dividerLoc(r.a.y, r.b.y, numRows + 1, y + 1);
        } else {
            nr.a.y = dividerLoc(r.a.y, r.b.y, numRows, y);
            nr.b.y = dividerLoc(r.a.y, r.b.y, numRows, y + 1);
        }
        return nr;
    }

    private static int[] mostEqualDivisors(int n, boolean favorY) {
        int i = (int) Math.sqrt(n);
        if (n % i != 0) {
            if (n % (i + 1) == 0) {
                i++;
            }
        }
        if (i < n / i) {
            i = n / i;
        }
        int x, y;
        if (favorY) {
            x = n / i;
            y = i;
        } else {
            y = n / i;
            x = i;
        }
        return new int[]{x, y};
    }

    public void tile(TRect r) {
        logger.trace("{} TDesktop@tile(r={})", getLogName(), r);

        AtomicInteger numTileable = new AtomicInteger(0);
        forEach(p -> {
            if (tileable(p)) {
                numTileable.incrementAndGet();
            }
        });

        if (numTileable.get() > 0) {
            int[] divs = mostEqualDivisors(numTileable.get(), !tileColumnsFirst);
            int numCols = divs[0];
            int numRows = divs[1];

            if ((r.b.x - r.a.x) / numCols == 0 || (r.b.y - r.a.y) / numRows == 0) {
                tileError();
            } else {
                int leftOver = numTileable.get() % numCols;
                AtomicInteger tileNum = new AtomicInteger(numTileable.get() - 1);
                lock();
                forEach(p -> {
                    if (tileable(p)) {
                        TRect nr = calcTileRect(tileNum.get(), r, numCols, numRows, leftOver);
                        p.locate(nr);
                        tileNum.decrementAndGet();
                    }
                });
                unlock();
            }
        }
    }

    public void tileError() {
        logger.error("{} TDesktop@tileError() Tile error", getLogName());
    }

}
