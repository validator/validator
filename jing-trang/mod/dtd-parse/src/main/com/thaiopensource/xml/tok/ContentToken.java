package com.thaiopensource.xml.tok;

/**
 * Represents information returned by <code>Tokenizer.tokenizeContent</code>.
 * @see Tokenizer#tokenizeContent
 */
public class ContentToken extends Token {
  private static final int INIT_ATT_COUNT = 8;
  private int attCount = 0;
  private int[] attNameStart = new int[INIT_ATT_COUNT];
  private int[] attNameEnd = new int[INIT_ATT_COUNT];
  private int[] attValueStart = new int[INIT_ATT_COUNT];
  private int[] attValueEnd = new int[INIT_ATT_COUNT];
  private boolean[] attNormalized = new boolean[INIT_ATT_COUNT];

  /**
   * Returns the number of attributes specified in the start-tag
   * or empty element tag.
   */
  public final int getAttributeSpecifiedCount() {
    return attCount;
  }

  /**
   * Returns the index of the first character of the name of the
   * attribute index <code>i</code>.
   */
  public final int getAttributeNameStart(int i) {
    if (i >= attCount)
      throw new IndexOutOfBoundsException();
    return attNameStart[i];
  }

  /**
   * Returns the index following the last character of the name of the
   * attribute index <code>i</code>.
   */
  public final int getAttributeNameEnd(int i) {
    if (i >= attCount)
      throw new IndexOutOfBoundsException();
    return attNameEnd[i];
  }

  /**
   * Returns the index of the character following the opening quote of
   * attribute index <code>i</code>.
   */
  public final int getAttributeValueStart(int i) {
    if (i >= attCount)
      throw new IndexOutOfBoundsException();
    return attValueStart[i];
  }

  /**
   * Returns the index of the closing quote attribute index <code>i</code>.
   */
  public final int getAttributeValueEnd(int i) {
    if (i >= attCount)
      throw new IndexOutOfBoundsException();
    return attValueEnd[i];
  }

  /**
   * Returns true if attribute index <code>i</code> does not need to
   * be normalized.  This is an optimization that allows further processing
   * of the attribute to be avoided when it is known that normalization
   * cannot change the value of the attribute.
   */
  public final boolean isAttributeNormalized(int i) {
    if (i >= attCount)
      throw new IndexOutOfBoundsException();
    return attNormalized[i];
  }

  final void clearAttributes() {
    attCount = 0;
  }
  
  final void appendAttribute(int nameStart, int nameEnd,
			     int valueStart, int valueEnd,
			     boolean normalized) {
    if (attCount == attNameStart.length) {
      attNameStart = grow(attNameStart);
      attNameEnd = grow(attNameEnd);
      attValueStart = grow(attValueStart);
      attValueEnd = grow(attValueEnd);
      attNormalized = grow(attNormalized);
    }
    attNameStart[attCount] = nameStart;
    attNameEnd[attCount] = nameEnd;
    attValueStart[attCount] = valueStart;
    attValueEnd[attCount] = valueEnd;
    attNormalized[attCount] = normalized;
    ++attCount;
  }

  final void checkAttributeUniqueness(char[] buf) throws InvalidTokenException {
    for (int i = 1; i < attCount; i++) {
      int len = attNameEnd[i] - attNameStart[i];
      for (int j = 0; j < i; j++) {
	if (attNameEnd[j] - attNameStart[j] == len) {
	  int n = len;
	  int s1 = attNameStart[i];
	  int s2 = attNameStart[j];
	  do {
	    if (--n < 0)
	      throw new InvalidTokenException(attNameStart[i],
					      InvalidTokenException.DUPLICATE_ATTRIBUTE);
	  } while (buf[s1++] == buf[s2++]);
	}
      }
    }
  }

  private static final int[] grow(int[] v) {
    int[] tem = v;
    v = new int[tem.length << 1];
    System.arraycopy(tem, 0, v, 0, tem.length);
    return v;
  }

  private static final boolean[] grow(boolean[] v) {
    boolean[] tem = v;
    v = new boolean[tem.length << 1];
    System.arraycopy(tem, 0, v, 0, tem.length);
    return v;
  }

}
