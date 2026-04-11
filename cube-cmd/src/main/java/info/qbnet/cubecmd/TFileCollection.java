package info.qbnet.cubecmd;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public class TFileCollection {

    public enum SortMode {
        NAME,
        EXTENSION,
        SIZE,
        TIME,
        UNSORTED
    }

    public enum PanelFlag {
        DIRECTORIES_FIRST,
        EXECUTABLE_FIRST,
        ARCHIVES_FIRST
    }

    private static final Set<String> EXECUTABLE_EXTENSIONS = Set.of("exe", "bat", "cmd", "com", "sh");
    private static final Set<String> ARCHIVE_EXTENSIONS = Set.of(
            "zip", "rar", "7z", "tar", "gz", "bz2", "xz", "tgz", "tbz", "jar");

    private final List<TFileRec> items = new ArrayList<>();
    private final List<TFileRec> viewItems = new ArrayList<>();
    private SortMode sortMode = SortMode.NAME;
    private EnumSet<PanelFlag> panelFlags = EnumSet.of(PanelFlag.DIRECTORIES_FIRST);
    private String fileMask = "*.*";
    private int selected = -1;
    private boolean dirty = true;

    public TFileCollection() {
    }

    public void add(TFileRec fileRec) {
        items.add(fileRec);
        dirty = true;
    }

    public void clear() {
        items.clear();
        viewItems.clear();
        selected = -1;
        dirty = false;
    }

    public TFileRec visibleGet(int idx) {
        ensureView();
        return viewItems.get(idx);
    }

    public int visibleSize() {
        ensureView();
        return viewItems.size();
    }

    public TFileRec rawGet(int idx) {
        return items.get(idx);
    }

    public int rawSize() {
        return items.size();
    }

    public void setSortMode(SortMode sortMode) {
        this.sortMode = sortMode;
        dirty = true;
    }

    public SortMode getSortMode() {
        return sortMode;
    }

    public void setPanelFlags(EnumSet<PanelFlag> panelFlags) {
        this.panelFlags = panelFlags.isEmpty() ? EnumSet.noneOf(PanelFlag.class) : EnumSet.copyOf(panelFlags);
        dirty = true;
    }

    public EnumSet<PanelFlag> getPanelFlags() {
        return EnumSet.copyOf(panelFlags);
    }

    public void setFileMask(String fileMask) {
        this.fileMask = (fileMask == null || fileMask.isBlank()) ? "*.*" : fileMask;
        dirty = true;
    }

    public String getFileMask() {
        return fileMask;
    }

    public int getSelected() {
        return selected;
    }

    public void setSelected(int selected) {
        ensureView();
        if (viewItems.isEmpty()) {
            this.selected = -1;
            return;
        }
        this.selected = Math.max(0, Math.min(selected, viewItems.size() - 1));
    }

    public void refresh() {
        dirty = true;
        ensureView();
    }

    private void ensureView() {
        if (!dirty) {
            return;
        }

        viewItems.clear();
        Pattern fileMaskPattern = buildFileMaskPattern(fileMask);

        for (TFileRec rec : items) {
            if (rec.isDirectory() || fileMaskPattern.matcher(rec.getName()).matches()) {
                viewItems.add(rec);
            }
        }

        if (sortMode != SortMode.UNSORTED) {
            viewItems.sort(buildComparator());
        }

        if (viewItems.isEmpty()) {
            selected = -1;
        } else if (selected < 0) {
            selected = 0;
        } else if (selected >= viewItems.size()) {
            selected = viewItems.size() - 1;
        }
        dirty = false;
    }

    private Comparator<TFileRec> buildComparator() {
        Comparator<TFileRec> cmp = Comparator.comparing((TFileRec rec) -> rec.getName().startsWith(".") ? 0 : 1);

        if (panelFlags.contains(PanelFlag.DIRECTORIES_FIRST)) {
            cmp = cmp.thenComparing(rec -> rec.isDirectory() ? 0 : 1);
        }
        if (panelFlags.contains(PanelFlag.EXECUTABLE_FIRST)) {
            cmp = cmp.thenComparing(rec -> isExecutable(rec) ? 0 : 1);
        }
        if (panelFlags.contains(PanelFlag.ARCHIVES_FIRST)) {
            cmp = cmp.thenComparing(rec -> isArchive(rec) ? 0 : 1);
        }

        cmp = cmp.thenComparing(switch (sortMode) {
            case EXTENSION -> Comparator.comparing(TFileRec::getExtension, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(TFileRec::getName, String.CASE_INSENSITIVE_ORDER);
            case SIZE -> Comparator.comparingLong(TFileRec::getSize)
                    .thenComparing(TFileRec::getName, String.CASE_INSENSITIVE_ORDER);
            case TIME -> Comparator.comparingLong(TFileRec::getTimestamp)
                    .reversed()
                    .thenComparing(TFileRec::getName, String.CASE_INSENSITIVE_ORDER);
            case UNSORTED, NAME -> Comparator.comparing(TFileRec::getName, String.CASE_INSENSITIVE_ORDER);
        });

        return cmp;
    }

    private static Pattern buildFileMaskPattern(String fileMask) {
        StringBuilder regex = new StringBuilder("^");
        for (char ch : fileMask.toLowerCase(Locale.ROOT).toCharArray()) {
            switch (ch) {
                case '*' -> regex.append(".*");
                case '?' -> regex.append(".");
                case '.', '\\', '+', '(', ')', '[', ']', '{', '}', '^', '$', '|' -> regex.append('\\').append(ch);
                default -> regex.append(ch);
            }
        }
        regex.append("$");
        return Pattern.compile(regex.toString(), Pattern.CASE_INSENSITIVE);
    }

    private static boolean isExecutable(TFileRec rec) {
        return !rec.isDirectory() && EXECUTABLE_EXTENSIONS.contains(rec.getExtension());
    }

    private static boolean isArchive(TFileRec rec) {
        return !rec.isDirectory() && ARCHIVE_EXTENSIONS.contains(rec.getExtension());
    }

}
