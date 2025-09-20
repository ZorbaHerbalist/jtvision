package info.qbnet.jtvision.util;

import java.util.Objects;

/**
 * Descriptor providing convenient access to palettes registered via
 * {@link PaletteFactory}. A descriptor encapsulates the palette name together
 * with the palette role enum, exposes the lazily constructed {@link TPalette}
 * instance and offers helper methods for resolving palette roles to their
 * configured color values.
 *
 * <p>
 * Descriptors can also hold a temporary palette override which makes it easy
 * to inject custom palette data during tests without mutating the global
 * factory state.
 * </p>
 *
 * @param <R> palette role enum type
 */
public final class PaletteDescriptor<R extends Enum<R> & PaletteRole> {

    private final String name;
    private final Class<R> roleEnum;
    private volatile TPalette override;

    private PaletteDescriptor(String name, Class<R> roleEnum) {
        this.name = name;
        this.roleEnum = roleEnum;
    }

    /**
     * Registers the palette {@code name} using JSON definitions and returns a descriptor for convenient access.
     */
    public static <R extends Enum<R> & PaletteRole> PaletteDescriptor<R> register(String name, Class<R> roleEnum) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(roleEnum, "roleEnum");
        PaletteFactory.register(name, roleEnum);
        return new PaletteDescriptor<>(name, roleEnum);
    }

    /** Returns the palette name used when resolving the descriptor. */
    public String name() {
        return name;
    }

    /** Returns the palette role enum backing this descriptor. */
    public Class<R> roleEnum() {
        return roleEnum;
    }

    /**
     * Returns the palette associated with this descriptor. When an override
     * palette has been provided via {@link #overridePalette(TPalette)}, the
     * override is returned, otherwise the palette is retrieved from the
     * {@link PaletteFactory} cache.
     */
    public TPalette palette() {
        TPalette current = override;
        if (current != null) {
            return current;
        }
        return PaletteFactory.get(name);
    }

    /**
     * Resolves {@code role} to the configured color value in this palette.
     */
    public byte color(R role) {
        Objects.requireNonNull(role, "role");
        return palette().get(role);
    }

    /**
     * Convenience method returning the packed color pair corresponding to
     * {@code normalRole}. The highlight byte remains unset.
     */
    public short colorPair(R normalRole) {
        return colorPair(normalRole, null);
    }

    /**
     * Convenience method returning the packed color pair corresponding to the
     * supplied roles.
     */
    public short colorPair(R normalRole, R highlightRole) {
        Objects.requireNonNull(normalRole, "normalRole");
        int normal = Byte.toUnsignedInt(color(normalRole));
        int highlight = highlightRole != null ? Byte.toUnsignedInt(color(highlightRole)) : 0;
        return (short) ((highlight << 8) | normal);
    }

    /**
     * Temporarily overrides the palette returned by {@link #palette()}. The
     * supplied palette must not be {@code null}.
     */
    public void overridePalette(TPalette palette) {
        this.override = Objects.requireNonNull(palette, "palette");
    }

    /** Clears any palette override previously set via {@link #overridePalette(TPalette)}. */
    public void clearOverride() {
        this.override = null;
    }

    /** Returns {@code true} when an override palette is active. */
    public boolean hasOverride() {
        return override != null;
    }
}
