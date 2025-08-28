package info.qbnet.jtvision.util;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Convenience wrapper around {@link ByteBuffer} for building and parsing
 * binary packets passed between dialogs.
 *
 * <p>The class exposes type specific {@code put} and {@code get} methods
 * similar to those of {@link ByteBuffer} but adds helpers for working with
 * strings encoded in UTF-8. Each string is stored as a length prefixed
 * sequence where the length is written as an unsigned short.</p>
 *
 * <pre>{@code
 * DataPacket packet = new DataPacket(64)
 *         .putString("Hello")
 *         .putShort((short) 1234)
 *         .rewind();
 * String text = packet.getString();
 * short number = packet.getShort();
 * }</pre>
 */
public class DataPacket {

    private final ByteBuffer buffer;
    private Charset charset = StandardCharsets.UTF_8;

    /**
     * Creates a new packet with the given capacity for writing.
     */
    public DataPacket(int capacity) {
        this.buffer = ByteBuffer.allocate(capacity);
    }

    /**
     * Wraps existing binary data for reading.
     */
    public DataPacket(byte[] data) {
        this.buffer = ByteBuffer.wrap(data);
    }

    /**
     * Writes a UTF-8 string prefixed with its length as an unsigned short.
     */
    public DataPacket putString(String value) {
        byte[] bytes = value.getBytes(charset);
        if (bytes.length > 0xFFFF) {
            throw new IllegalArgumentException("String too long: " + bytes.length);
        }
        buffer.putShort((short) bytes.length);
        buffer.put(bytes);
        return this;
    }

    /**
     * Reads a UTF-8 string that was encoded with {@link #putString(String)}.
     */
    public String getString() {
        int len = Short.toUnsignedInt(buffer.getShort());
        byte[] bytes = new byte[len];
        buffer.get(bytes);
        return new String(bytes, charset);
    }

    /**
     * Puts a single short value.
     */
    public DataPacket putShort(short value) {
        buffer.putShort(value);
        return this;
    }

    /**
     * Reads a short value.
     */
    public short getShort() {
        return buffer.getShort();
    }

    /**
     * Writes an int value.
     */
    public DataPacket putInt(int value) {
        buffer.putInt(value);
        return this;
    }

    /**
     * Reads an int value.
     */
    public int getInt() {
        return buffer.getInt();
    }

    /**
     * Rewinds the underlying buffer to prepare for reading.
     */
    public DataPacket rewind() {
        buffer.rewind();
        return this;
    }

    /**
     * Returns the contents of the buffer up to the current position.
     */
    public byte[] toByteArray() {
        int pos = buffer.position();
        byte[] data = new byte[pos];
        buffer.rewind();
        buffer.get(data);
        buffer.position(pos);
        return data;
    }

    /**
     * Exposes the underlying {@link ByteBuffer} for advanced operations.
     */
    public ByteBuffer getByteBuffer() {
        return buffer;
    }

    /**
     * Configures the charset used for string encoding/decoding.
     */
    public DataPacket setCharset(Charset charset) {
        this.charset = charset;
        return this;
    }
}

