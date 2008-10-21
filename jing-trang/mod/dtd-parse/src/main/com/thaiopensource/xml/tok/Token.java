package com.thaiopensource.xml.tok;

/**
 * Represents information returned by the tokenizing methods in
 * <code>Tokenizer</code>.
 *
 * @see Tokenizer#tokenizeContent
 * @see Tokenizer#tokenizeProlog
 * @see Tokenizer#tokenizeAttributeValue
 * @see Tokenizer#tokenizeEntityValue
 */
public class Token {
  int tokenEnd = -1;
  int nameEnd = -1;
  char refChar1 = 0;
  char refChar2 = 0;

  public final int getTokenEnd() {
    return tokenEnd;
  }

  protected final void setTokenEnd(int i) {
    tokenEnd = i;
  }

  public final int getNameEnd() {
    return nameEnd;
  }

  protected final void setNameEnd(int i) {
    nameEnd = i;
  }

  public final char getRefChar() {
    return refChar1;
  }

  public final void getRefCharPair(char[] ch, int off) {
    ch[off] = refChar1;
    ch[off + 1] = refChar2;
  }
}
