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
```
