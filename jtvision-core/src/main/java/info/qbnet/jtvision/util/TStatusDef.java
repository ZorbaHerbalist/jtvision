package info.qbnet.jtvision.util;

/**
 * Defines a set of {@link TStatusItem} objects applicable to a range of command values.
 */
public record TStatusDef(int min, int max, TStatusItem items, TStatusDef next) {
}

