package com.thaiopensource.xml.tok;

/**
 * Thrown to indicate that the subarray being tokenized is not the
 * complete encoding of one or more XML characters, but might be if
 * more chars were added.
 */
public class PartialCharException extends PartialTokenException {
  private final int leadCharIndex;
  PartialCharException(int leadCharIndex) {
    this.leadCharIndex = leadCharIndex;
  }
  /**
   * Returns the index of the first char that is not part of the complete
   * encoding of a character.
   */
  public int getLeadCharIndex() {
    return leadCharIndex;
  }
}
