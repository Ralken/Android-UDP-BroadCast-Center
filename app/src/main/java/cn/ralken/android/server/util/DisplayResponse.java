package cn.ralken.android.server.util;

import java.nio.ByteBuffer;

/**
 * Encapsulate the response for display device with a hexical string to represent
 * the results in a binary structure.
 *
 * @author zhenminli
 */
public class DisplayResponse {
  public static final short CURRENT_VERSION = 1; // must be less than 256
  public static final int HEADER_LEN = 5;

  private static final String HEXES = "0123456789ABCDEF";

  private final int capacity;
  private final ByteBuffer buffer;
  private int dataCount = 0;

  public static enum Command {
    SET_TIME, // 0: set display time
    CONFIG,   // 1: configuration
   // WEATHER,  // 2: display weather information
   // MEASURE   // 3: display measurement result
  }

  /**
   * Construct a display response using the maximum capacity of the data length.
   * @param capacity capacity of the buffer to hold data.
   * @param cmd the commands in the display response.
   */
  public DisplayResponse(int capacity, Command cmd) {
    this.capacity = capacity;
    this.buffer = ByteBuffer.allocate(HEADER_LEN + capacity + 1);
    fillHeader(cmd);
  }

  private void fillHeader(Command cmd) {
    buffer.put((byte)'M');
    buffer.put((byte)'O');
    buffer.put((byte)CURRENT_VERSION);
    buffer.put((byte)cmd.ordinal());
    buffer.put((byte)capacity);
  }

  /**
   * Put a byte in the result.
   * @param int8 byte
   * @return the display response.
   */
  public DisplayResponse putByte(byte int8) {
    buffer.put(int8);
    dataCount++;
    return this;
  }

  /**
   * Put a short integer number.
   * @param int16 short integer number
   * @return the display response.
   */
  public DisplayResponse putShort(short int16) {
    buffer.putShort(int16);
    dataCount += 2;
    return this;
  }

  /**
   * Put an integer number.
   * @param int32 integer number
   * @return the display response.
   */
  public DisplayResponse putInt(int int32) {
    buffer.putInt(int32);
    dataCount += 4;
    return this;
  }

  /**
   * Encode the display response with a hexical string.
   * @return the string that represents the response binary structure.
   */
  public String encodeToString() {
    buffer.put(HEADER_LEN - 1, (byte) dataCount); // fill in data payload length
    int totalLen = HEADER_LEN + dataCount; // exclude CRC byte
    byte[] bytes = buffer.array();
    bytes[totalLen] = calculateCrc(bytes, totalLen); // fill in CRC
    return encodeHex(bytes, totalLen + 1);
  }

  static String encodeHex(byte[] bytes, int len) {
    StringBuilder hex = new StringBuilder(2 * len);
    for (int i = 0; i < len; i++) {
      byte b = bytes[i];
      hex.append(HEXES.charAt((b & 0xF0) >> 4))
          .append(HEXES.charAt((b & 0x0F)));
    }
    return hex.toString();
  }

  static byte calculateCrc(byte[] bytes, int len) {
    int sum = 0;
    for (int i = 0; i < len; i++) {
      int b = bytes[i];
      sum +=  (b & 0xff);
    }
    return (byte)sum;
  }
}
