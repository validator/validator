package com.thaiopensource.xml.dtd;

public final class Buffer {
  private static final int INIT_SIZE = 64;
  private char[] buf = new char[INIT_SIZE];
  private int len;

  public void clear() {
    len = 0;
  }

  public void append(char c) {
    need(1);
    buf[len++] = c;
  }

  public void appendRefCharPair(Token t) {
    need(2);
    t.getRefCharPair(buf, len);
    len += 2;
  }

  public void append(char[] cbuf, int start, int end) {
    need(end - start);
    for (int i = start; i < end; i++)
      buf[len++] = cbuf[i];
  }

  private void need(int n) {
    if (len + n <= buf.length)
      return;
    char[] tem = buf;
    if (n > tem.length)
      buf = new char[n * 2];
    else
      buf = new char[tem.length << 1];
    System.arraycopy(tem, 0, buf, 0, tem.length);
  }

  public char[] getChars() {
    char[] text = new char[len];
    System.arraycopy(buf, 0, text, 0, len);
    return text;
  }

  public String toString() {
    return new String(buf, 0, len);
  }

  public int length() {
    return len;
  }

  public char charAt(int i) {
    if (i >= len)
      throw new IndexOutOfBoundsException();
    return buf[i];
  }

  public void chop() {
    --len;
  }
}
