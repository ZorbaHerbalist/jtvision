package info.qbnet.jtvision.core.views;

public class TPalette {

    private final byte[] data;

    public TPalette(byte[] data) {
        this.data = data != null ? data.clone() : new byte[0];
    }

    /**
     * Returns the number of colors in this palette.
     */
    public int length() {
        return data.length;
    }

    /**
     * Returns the palette entry at the specified index.
     *
     * @param index index of the color to return
     * @return color value at the given index
     * @throws ArrayIndexOutOfBoundsException if the index is out of range
     */
    public byte get(int index) {
        return data[index];
    }

    /**
     * Exposes the underlying palette data.
     */
    public byte[] getData() {
        return data.clone();
    }

}
