package info.qbnet.jtvision.util;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class TPalette {

    private final Map<PaletteRole, Byte> roleData;
    private final Map<Integer, Byte> indexData;

    public <R extends Enum<R> & PaletteRole> TPalette(EnumMap<R, Byte> data) {
        Objects.requireNonNull(data, "palette data");
        Map<PaletteRole, Byte> roleCopy = new LinkedHashMap<>();
        Map<Integer, Byte> indexCopy = new HashMap<>();
        data.forEach((role, value) -> {
            roleCopy.put(role, value);
            int index = role.index();
            if (index <= 0) {
                throw new IllegalArgumentException("Palette role index must be positive: " + role);
            }
            Byte previous = indexCopy.put(index, value);
            if (previous != null) {
                throw new IllegalArgumentException("Duplicate palette index " + index + " for role " + role);
            }
        });
        this.roleData = Collections.unmodifiableMap(roleCopy);
        this.indexData = Collections.unmodifiableMap(indexCopy);
    }

    /**
     * Returns the number of colors in this palette.
     */
    public int length() {
        return indexData.size();
    }

    /**
     * Returns the palette entry at the specified index.
     *
     * @param index index of the color to return
     * @return color value at the given index
     * @throws ArrayIndexOutOfBoundsException if the index is out of range
     */
    public byte get(int index) {
        Byte value = indexData.get(index);
        if (value == null) {
            throw new ArrayIndexOutOfBoundsException("Palette index " + index + " out of range");
        }
        return value;
    }

    /**
     * Returns the palette entry at the specified index, or {@code null} when the
     * palette does not define the index.
     */
    public Byte getOrNull(int index) {
        return indexData.get(index);
    }

    /**
     * Returns the palette entry matching the given role.
     */
    public byte get(PaletteRole role) {
        Byte value = roleData.get(role);
        if (value == null) {
            throw new IllegalArgumentException("Palette role not defined: " + role);
        }
        return value;
    }

    /**
     * Checks whether the palette defines a value for the requested index.
     */
    public boolean containsIndex(int index) {
        return indexData.containsKey(index);
    }

    /**
     * Exposes a read-only view of the palette entries keyed by role.
     */
    public Map<PaletteRole, Byte> getData() {
        return roleData;
    }

}
