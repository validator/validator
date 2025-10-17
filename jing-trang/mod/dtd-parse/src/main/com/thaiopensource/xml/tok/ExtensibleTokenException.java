package com.thaiopensource.xml.tok;

/**
 * Thrown to indicate that the char subarray being tokenized is a legal XML
 * token, but that subsequent chars in the same entity could be part of
 * the token.  For example, <code>Tokenizer.tokenizeProlog</code>
 * would throw this if the char subarray consists of a legal XML name.
 */
public class ExtensibleTokenException extends TokenException {
  private final int tokType;

  ExtensibleTokenException(int tokType) {
    this.tokType = tokType;
  }

  /**
   * Returns the type of token in the byte subarrary.
   */
  public int getTokenType() {
    return tokType;
  }
}
