package info.qbnet.jtvision.util;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Simple in-memory replacement for Turbo Vision's history buffer.
 * <p>
 * The original implementation stores all history strings in a contiguous
 * memory block manipulated through assembler routines. For the Java port we
 * model each history list as a deque keyed by its identifier. The behaviour is
 * intentionally conservative: duplicates are removed before the new value is
 * pushed to the front and empty strings are ignored.
 * </p>
 */
public final class HistoryList {

    private static final Map<Integer, Deque<String>> ENTRIES = new HashMap<>();

    private HistoryList() {
        // utility class
    }

    /**
     * Adds {@code value} to the history identified by {@code id}.
     * Duplicate entries are moved to the front.
     */
    public static synchronized void add(int id, String value) {
        if (value == null || value.isEmpty()) {
            return;
        }

        Deque<String> history = ENTRIES.computeIfAbsent(id, k -> new ArrayDeque<>());

        for (Iterator<String> it = history.iterator(); it.hasNext(); ) {
            if (value.equals(it.next())) {
                it.remove();
            }
        }
        history.addFirst(value);
    }

    /** Returns the number of entries stored for {@code id}. */
    public static synchronized int count(int id) {
        Deque<String> history = ENTRIES.get(id);
        return history != null ? history.size() : 0;
    }

    /**
     * Retrieves the entry at {@code index} (0-based) for history {@code id}.
     * Returns {@code null} if the history is shorter than {@code index + 1}.
     */
    public static synchronized String get(int id, int index) {
        Deque<String> history = ENTRIES.get(id);
        if (history == null || index < 0 || index >= history.size()) {
            return null;
        }

        int i = 0;
        for (String entry : history) {
            if (i++ == index) {
                return entry;
            }
        }
        return null;
    }

    /** Clears all stored history data. */
    public static synchronized void clear() {
        ENTRIES.clear();
    }
}