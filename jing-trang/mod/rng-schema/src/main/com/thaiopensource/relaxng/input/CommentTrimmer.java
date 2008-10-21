package com.thaiopensource.relaxng.input;

public class CommentTrimmer {
  private CommentTrimmer() {
  }

  public static String trimComment(String value) {
    return trim(unindent(value));
  }

  private static String trim(String value) {
    int len = value.length();
    loop1:
    for (; len > 0; --len) {
      switch (value.charAt(len - 1)) {
      case ' ':
      case '\t':
        break;
      case '\n':
        --len;
        break loop1;
      default:
        break loop1;
      }
    }
    int start = 0;
    loop2:
    for (; start < len; start++) {
      switch (value.charAt(start)) {
      case ' ':
      case '\t':
        break;
      case '\n':
        ++start;
        break loop2;
      default:
        break loop2;
      }
    }
    if (start < 0 || len < value.length())
      return value.substring(start, len);
    return value;
  }

  private static String unindent(String value) {
    int minIndent = -1;
    boolean usedTabs = false;
    for (int i = value.indexOf('\n'), len = value.length(); i >= 0; i = value.indexOf('\n', i)) {
      ++i;
      int currentIndent = 0;
      loop:
      for (; i < len; i++) {
        switch (value.charAt(i)) {
        case '\n':
          currentIndent = 0;
          break;
        case ' ':
          ++currentIndent;
          break;
        case '\t':
          currentIndent = ((currentIndent/8) + 1)*8;
          usedTabs = true;
          break;
        default:
          break loop;
        }
      }
      if (i >= len)
        break;
      if (currentIndent < minIndent || minIndent < 0)
        minIndent = currentIndent;
    }
    if (minIndent < 0)
      return value;
    StringBuffer buf = new StringBuffer();
    int currentIndent = -1;
    for (int i = 0, len = value.length(); i < len; i++) {
      char c = value.charAt(i);
      switch (c) {
      case ' ':
        if (currentIndent >= 0)
          currentIndent++;
        else
          buf.append(c);
        break;
      case '\t':
        if (currentIndent >= 0)
          currentIndent = ((currentIndent/8) + 1)*8;
        else
          buf.append(c);
        break;
      case '\n':
        buf.append(c);
        currentIndent = 0;
        break;
      default:
        if (currentIndent > minIndent) {
          currentIndent -= minIndent;
          if (usedTabs) {
            while (currentIndent >= 8) {
              buf.append('\t');
              currentIndent -= 8;
            }
          }
          while (currentIndent > 0) {
            buf.append(' ');
            currentIndent--;
          }
        }
        currentIndent = -1;
        buf.append(c);
        break;
      }
    }
    return buf.toString();
  }
}