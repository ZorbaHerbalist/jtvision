package info.qbnet.jtvision.core.views;

import info.qbnet.jtvision.core.objects.TRect;
import info.qbnet.jtvision.util.IBuffer;

import java.util.function.Predicate;

public class TGroup extends TView {

    protected TView last = null;
    protected TView current = null;
    private IBuffer buffer = null;

    protected TRect clip = new TRect(0, 0, 0, 0);
    private int lockFlag = 0;

    enum SelectMode {
        NORMAL_SELECT,
        ENTER_SELECT,
        LEAVE_SELECT,
    }

    public TGroup(TRect bounds) {
        super(bounds);
        getExtent(clip);

        logger.debug("{} TGroup@TGroup(bounds={})", getLogName(), bounds);
    }

    public IBuffer getBuffer() {
        logger.trace("{} TGroup@getBuffer()", getLogName());

        return buffer;
    }

    public void setBuffer(IBuffer buffer) {
        logger.trace("{} TGroup@setBuffer({})", getLogName(), buffer);

        this.buffer = buffer;
    }

    @Override
    public void draw() {
        logger.trace("{} TGroup@draw()", getLogName());

        super.draw();
        // TODO
    }

    void drawSubViews(TView p, TView bottom) {
        if (p != null) {
            while (p != bottom) {
                p.drawView();
                p = p.getNext();
            }
        }
    }

    public TView firstMatch(int state, int options) {
        return firstThat(v -> (v.getState() & state) == state && (v.getOptions() & options) == options);
    }

    public TView firstThat(Predicate<TView> predicate) {
        if (last == null) return null;

        TView current = last.getNext();
        do {
            if (predicate.test(current)) return current;
            current = current.getNext();
        } while (current != last.getNext());

        return null;
    }

    public void insert(TView view) {
        logger.trace("{} TGroup@insert({})", getLogName(), view != null ? view.getLogName() : "null");

        insertBefore(view, getFirst());
    }

    public void insertBefore(TView p, TView target) {
        logger.trace("{} TGroup@insertBefore({}, {})", getLogName(), p != null ? p.getLogName() : "null",
                target != null ? target.getLogName() : "null");

        if (p != null && p.getOwner() == null && (target == null || target.getOwner() == this)) {
            if ((p.getOptions() & Options.OF_CENTER_X) != 0) {
                p.origin.x = (size.x - p.size.x) / 2;
            }
            if ((p.getOptions() & Options.OF_CENTER_Y) != 0) {
                p.origin.y = (size.y - p.size.y) / 2;
            }
            int saveState = p.getState();
            p.hide();
            // TODO
        }

        if (target == null) {
            last = p;
        } else {
            target.setOwner(this);
            target.setNext(p);
        }
        p.setOwner(this);
        p.setNext(target);
    }

    public void lock() {
        logger.trace("{} TGroup@lock()", getLogName());

        if (buffer != null || lockFlag != 0) {
            lockFlag++;
        }
    }

    public void unlock() {
        logger.trace("{} TGRoup@unlock()", getLogName());

        if (lockFlag != 0) {
            lockFlag--;
            if (lockFlag == 0) {
                drawView();
            }
        }
    }

    protected void resetCurrent() {
        logger.trace("{} TGroup@resetCurrent()", getLogName());

        setCurrent(firstMatch(State.SF_VISIBLE, Options.OF_SELECTABLE), SelectMode.NORMAL_SELECT);
    }

    private void focusView(TView v, boolean enable) {
        logger.trace("{} TGroup@focusView({}, {})", getLogName(), v != null ? v.getLogName() : "null", enable);

        if ((this.getState() & State.SF_FOCUSED) != 0 && v != null) {
            v.setState(State.SF_FOCUSED, enable);
        }
    }

    private void selectView(TView v, boolean enable) {
        logger.trace("{} TGroup@selectView({}, {})", getLogName(), v != null ? v.getLogName() : "null", enable);

        if (v != null) {
            v.setState(State.SF_SELECTED, enable);
        }
    }

    private void setCurrent(TView v, SelectMode mode) {
        logger.trace("{} TGroup@setCurrent({}, {})", getLogName(), v != null ? v.getLogName() : "null", mode);

        if (current != v) {
            lock();
            focusView(current, false);
            if (mode == SelectMode.ENTER_SELECT) {
                selectView(current, false);
            }
            if (mode == SelectMode.LEAVE_SELECT) {
                selectView(v, true);
            }
            focusView(v, true);
            current = v;
            unlock();
        }
    }

    public TView getFirst() {
        if (last == null)
            return null;
        else
            return last.getNext();
    }

}
