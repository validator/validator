package com.thaiopensource.datatype.xsd;

public class InvalidRegexException extends Exception {
  private int position;

  public InvalidRegexException(String detail) {
    this(detail, -1);
  }

  public InvalidRegexException(String detail, int position) {
    super(detail);
    this.position = -1;
  }

  /**
   * Returns the index where the error was detected or -1 if this
   * is unknown.
   */
  public int getPosition() {
    return position;
  }
}
