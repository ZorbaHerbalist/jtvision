package info.qbnet.jtvision.util;

/**
 * Represents a single item in a {@link info.qbnet.jtvision.views.TStatusLine}.
 * Status line items form a linked list.
 */
public record TStatusItem(String text, int keyCode, int command, TStatusItem next) {
}

