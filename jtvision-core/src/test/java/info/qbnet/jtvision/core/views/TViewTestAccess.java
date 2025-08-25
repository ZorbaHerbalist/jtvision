package info.qbnet.jtvision.core.views;

/**
 * Test-only accessors for {@link TView} package-private methods.
 */
public final class TViewTestAccess {
    private TViewTestAccess() {}

    /**
     * Sets option flags on a view.
     *
     * @param view  view to configure
     * @param flags option flags
     */
    public static void setOptions(TView view, int flags) {
        view.setOptions(flags);
    }
}
