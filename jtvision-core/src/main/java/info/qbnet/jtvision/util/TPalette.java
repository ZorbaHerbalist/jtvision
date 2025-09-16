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

    public static byte[] parseHexString(String s) {
        s = s.replaceAll("\\s+", "");
        String[] parts = s.split("\\\\x");
        byte[] data = new byte[parts.length - 1]; // pierwszy element jest pusty
        for (int i = 1; i < parts.length; i++) {
            data[i - 1] = (byte) Integer.parseInt(parts[i], 16);
        }
        return data;
    }

    public static <R extends Enum<R> & PaletteRole> EnumMap<R, Byte> mapFromHexString(String s, R[] roles) {
        byte[] values = parseHexString(s);
        if (roles.length != values.length) {
            throw new IllegalArgumentException("Palette definition and role count mismatch: expected "
                    + roles.length + " values but found " + values.length);
        }
        if (roles.length == 0) {
            throw new IllegalArgumentException("Palette must define at least one role");
        }
        EnumMap<R, Byte> map = new EnumMap<>(roles[0].getDeclaringClass());
        for (int i = 0; i < roles.length; i++) {
            map.put(roles[i], values[i]);
        }
        return map;
    }

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
