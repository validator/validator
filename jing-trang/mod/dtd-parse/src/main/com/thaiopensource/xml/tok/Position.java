package com.thaiopensource.xml.tok;

/**
 * Represents a position in an entity.
 * A position can be modified by <code>Tokenizer.movePosition</code>.
 * @see Tokenizer#movePosition
 */
public final class Position implements Cloneable {
  int lineNumber;
  int columnNumber;

  /**
   * Creates a position for the start of an entity: the line number is
   * 1 and the column number is 0.
   */
  public Position() {
    lineNumber = 1;
    columnNumber = 0;
  }

  /**
   * Returns the line number.
   * The first line number is 1.
   */
  public int getLineNumber() {
    return lineNumber;
  }

  /**
   * Returns the column number.
   * The first column number is 0.
   * A tab character is not treated specially.
   */
  public int getColumnNumber() {
    return columnNumber;
  }

  /**
   * Returns a copy of this position.
   */
  public Object clone() {
    try {
      return super.clone();
    }
    catch (CloneNotSupportedException e) {
      throw new InternalError();
    }
  }
}
