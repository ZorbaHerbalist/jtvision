package info.qbnet.jtvision.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
        this.buffer = ByteBuffer.allocate(capacity).order(ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * Wraps existing binary data for reading.
     */
    public DataPacket(byte[] data) {
        this.buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
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
     * Writes a length-prefixed string occupying a fixed-size field.
     * <p>
     * The method stores the UTF-8 encoded {@code value} prefixed with its
     * length as an unsigned short. The field is padded with zero bytes to the
     * requested {@code fieldSize} to ensure that subsequent writes start at the
     * expected offset. If the encoded string does not fit, it is truncated to
     * {@code fieldSize - 2} bytes and the stored length reflects the truncated
     * value.
     * </p>
     *
     * @param value     string to encode
     * @param fieldSize total number of bytes reserved for the field including
     *                  the two-byte length prefix
     * @return this packet for chaining
     * @throws IllegalArgumentException if {@code fieldSize} is less than 2
     */
    public DataPacket putStringField(String value, int fieldSize) {
        if (fieldSize < 2) {
            throw new IllegalArgumentException("fieldSize must be at least 2");
        }
        if (fieldSize > buffer.remaining()) {
            throw new java.nio.BufferOverflowException();
        }

        byte[] bytes = value.getBytes(charset);
        int maxBytes = fieldSize - 2;
        int len = Math.min(bytes.length, maxBytes);
        buffer.putShort((short) len);
        buffer.put(bytes, 0, len);
        for (int i = len; i < maxBytes; i++) {
            buffer.put((byte) 0);
        }
        return this;
    }

    /**
     * Reads a string written with {@link #putStringField(String, int)}.
     * <p>
     * The method consumes {@code fieldSize} bytes from the underlying buffer,
     * returning at most {@code fieldSize - 2} bytes decoded using the current
     * charset. Any remaining padding within the field is skipped.
     * </p>
     *
     * @param fieldSize size of the field including the length prefix
     * @return decoded string (may be shorter than the stored length if the
     *         buffer does not contain enough bytes)
     * @throws IllegalArgumentException if {@code fieldSize} is less than 2
     */
    public String getStringField(int fieldSize) {
        if (fieldSize < 2) {
            throw new IllegalArgumentException("fieldSize must be at least 2");
        }

        int available = Math.min(fieldSize, buffer.remaining());
        int fieldEnd = buffer.position() + available;
        if (available < 2) {
            buffer.position(fieldEnd);
            return "";
        }

        int len = Short.toUnsignedInt(buffer.getShort());
        int dataAvailable = Math.min(len, Math.max(0, fieldEnd - buffer.position()));
        byte[] bytes = new byte[dataAvailable];
        buffer.get(bytes);
        buffer.position(fieldEnd);
        return new String(bytes, charset);
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

