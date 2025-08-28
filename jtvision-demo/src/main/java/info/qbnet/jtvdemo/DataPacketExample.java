package info.qbnet.jtvdemo;

import info.qbnet.jtvision.util.DataPacket;

/**
 * Demonstrates usage of {@link DataPacket} for building a simple binary
 * record that stores a string and a number.
 */
public final class DataPacketExample {

    private DataPacketExample() {
        // utility class
    }

    public static void main(String[] args) {
        DataPacket packet = new DataPacket(32)
                .putString("Demo")
                .putShort((short) 7)
                .rewind();

        String text = packet.getString();
        short number = packet.getShort();

        System.out.printf("Decoded: %s %d%n", text, number);
    }
}
