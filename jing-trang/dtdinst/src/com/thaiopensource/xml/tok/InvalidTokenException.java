package com.thaiopensource.xml.tok;

/**
 * Thrown to indicate that the byte subarray being tokenized does not start
 * with a legal XML token and cannot start one if more bytes are added.
 */
public class InvalidTokenException extends TokenException {
  private final int offset;

  /**
   * The character or byte at the specified offset is not allowed
   * at that point.
   */
  public static final byte ILLEGAL_CHAR = 0;
  /**
   * The target of a processing instruction was XML.
   */
  public static final byte XML_TARGET = 1;
  /**
   * A duplicate attribute was specified.
   */
  public static final byte DUPLICATE_ATTRIBUTE = 2;

  private final byte type;
  
  InvalidTokenException(int offset, byte type) {
    this.offset = offset;
    this.type = type;
  }

  InvalidTokenException(int offset) {
    this.offset = offset;
    this.type = ILLEGAL_CHAR;
  }

  /**
   * Returns the offset after the longest initial subarray
   * which could start a legal XML token.
   */
  public final int getOffset() {
    return offset;
  }
  public final byte getType() {
    return type;
  }
}
