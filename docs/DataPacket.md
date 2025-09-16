# DataPacket

`DataPacket` to klasa pomocnicza ułatwiająca budowanie binarnych pakietów z
wykorzystaniem `ByteBuffer`.

## Przykład

```java
DataPacket packet = new DataPacket(64)
        .putString("Hello")
        .putShort((short) 12)
        .rewind();

String message = packet.getString();
short number = packet.getShort();

// Fixed-length field with a length prefix and zero padding
DataPacket dialogDefaults = new DataPacket(input.dataSize())
        .putShort((short) 2)
        .putStringField("*.txt", input.dataSize())
        .rewind();
```
