package info.qbnet.jtvision.util;

import info.qbnet.jtvision.views.TView;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Simple little-endian stream wrapper used for persisting {@link TView} instances.
 */
public class TStream {

    private final InputStream in;
    private final OutputStream out;

    /**
     * Map of registered view factories indexed by type identifier.
     */
    private static final Map<Integer, Function<TStream, TView>> TYPES = new HashMap<>();

    /**
     * Creates a stream for reading.
     *
     * @param in input source
     */
    public TStream(InputStream in) {
        this(in, null);
    }

    /**
     * Creates a stream for writing.
     *
     * @param out output destination
     */
    public TStream(OutputStream out) {
        this(null, out);
    }

    /**
     * Creates a stream for reading and writing.
     *
     * @param in  input source
     * @param out output destination
     */
    public TStream(InputStream in, OutputStream out) {
        this.in = in;
        this.out = out;
    }

    /**
     * Reads a 16-bit little-endian value from the underlying stream.
     *
     * @return the value read
     * @throws IOException if an I/O error occurs
     */
    public short readShort() throws IOException {
        int lo = in.read();
        int hi = in.read();
        if ((lo | hi) < 0) {
            throw new EOFException();
        }
        return (short) ((hi << 8) | lo);
    }

    /**
     * Reads a 32-bit little-endian integer from the stream.
     *
     * @return the value read
     * @throws IOException if an I/O error occurs
     */
    public int readInt() throws IOException {
        int b1 = in.read();
        int b2 = in.read();
        int b3 = in.read();
        int b4 = in.read();
        if ((b1 | b2 | b3 | b4) < 0) {
            throw new EOFException();
        }
        return (b4 << 24) | (b3 << 16) | (b2 << 8) | b1;
    }

    /**
     * Writes a 16-bit little-endian value to the stream.
     *
     * @param value value to write
     * @throws IOException if an I/O error occurs
     */
    public void writeShort(short value) throws IOException {
        out.write(value & 0xFF);
        out.write((value >>> 8) & 0xFF);
    }

    /**
     * Writes a 32-bit little-endian value to the stream.
     *
     * @param value value to write
     * @throws IOException if an I/O error occurs
     */
    public void writeInt(int value) throws IOException {
        out.write(value & 0xFF);
        out.write((value >>> 8) & 0xFF);
        out.write((value >>> 16) & 0xFF);
        out.write((value >>> 24) & 0xFF);
    }

    /**
     * Reads {@code count} bytes from the stream.
     *
     * @param count number of bytes to read
     * @return byte array of size {@code count}
     * @throws IOException if an I/O error occurs or end of stream is reached prematurely
     */
    public byte[] readBytes(int count) throws IOException {
        byte[] data = new byte[count];
        int off = 0;
        while (off < count) {
            int r = in.read(data, off, count - off);
            if (r < 0) {
                throw new EOFException();
            }
            off += r;
        }
        return data;
    }

    /**
     * Writes the provided bytes to the stream.
     *
     * @param data bytes to write
     * @throws IOException if an I/O error occurs
     */
    public void writeBytes(byte[] data) throws IOException {
        out.write(data);
    }

    /**
     * Registers a view factory for a given identifier.
     *
     * @param id      type identifier
     * @param factory constructor for the view
     */
    public static void registerType(int id, Function<TStream, TView> factory) {
        TYPES.put(id, factory);
    }

    /**
     * Loads a view from the stream.
     *
     * @return the loaded view
     * @throws IOException if an I/O error occurs
     */
    public TView loadView() throws IOException {
        int id = readInt();
        Function<TStream, TView> factory = TYPES.get(id);
        if (factory == null) {
            throw new IllegalStateException("Unknown view type: " + id);
        }
        return factory.apply(this);
    }

    /**
     * Stores a view to the stream.
     *
     * @param view view to store
     * @throws IOException if an I/O error occurs
     */
    public void storeView(TView view) throws IOException {
        writeInt(view.getClassId());
        view.store(this);
    }

    /**
     * Writes a UTF-8 encoded string prefixed with its length or -1 for {@code null}.
     */
    public void writeString(String value) throws IOException {
        if (value == null) {
            writeInt(-1);
        } else {
            byte[] data = value.getBytes(StandardCharsets.UTF_8);
            writeInt(data.length);
            writeBytes(data);
        }
    }

    /**
     * Reads a UTF-8 encoded string previously written with {@link #writeString}.
     */
    public String readString() throws IOException {
        int len = readInt();
        if (len < 0) {
            return null;
        }
        byte[] data = readBytes(len);
        return new String(data, StandardCharsets.UTF_8);
    }

}
