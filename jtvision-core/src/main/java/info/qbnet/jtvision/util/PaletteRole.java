package info.qbnet.jtvision.util;

/**
 * Identifies a symbolic role within a view palette. Roles are numbered using
 * the one-based indices used by Turbo Vision palettes so that legacy palette
 * definitions can be mapped directly onto the new enum based representation.
 */
public interface PaletteRole {

    /**
     * Returns the one-based palette index associated with this role.
     *
     * @return palette slot number (starting at 1)
     */
    int index();
}
