package com.thaiopensource.relaxng.output.dtd;

import java.util.NoSuchElementException;

class ModelBreaker {
  private boolean done = false;
  private BreakIterator iter;
  private final String prefix;
  private final String model;
  private final String suffix;
  private final int maxLineLength;
  private int modelPos;
  private ModelBreaker nested = null;

  private static class BreakIterator {
    private final String model;
    private int pos = 0;
    private final int length;

    BreakIterator(String model) {
      this.model = model;
      this.length = model.length();
    }

    int getPos() {
      return pos;
    }

    boolean advance() {
      boolean advanced = false;
      int level = 0;
      for (; pos < length; pos++) {
        switch (model.charAt(pos)) {
        case '(':
          level++;
          break;
        case ')':
          level--;
          break;
        case ',':
          if (level != 0)
            break;
          if (++pos != length)
            return true;
          break;
        case '|':
          if (level != 0 || !advanced)
            break;
          return true;
        case ' ':
          if (level != 0 || !advanced)
            break;
          return true;
        }
        advanced = true;
      }
      return false;
    }
  }

  ModelBreaker(String prefix, String model, String suffix, int maxLineLength) {
    if (isSingleGroup(model)) {
      int open = model.indexOf('(') + 1;
      this.prefix = prefix + model.substring(0, open);
      this.model = model.substring(open);
    }
    else {
      this.prefix = prefix;
      this.model = model;
    }
    this.suffix = suffix;
    this.maxLineLength = maxLineLength;
    this.modelPos = 0;
  }

  boolean hasNextLine() {
    if (nested != null && nested.hasNextLine())
      return true;
    return !done;
  }

  String nextLine() {
    if (nested != null && nested.hasNextLine())
      return nested.nextLine();
    if (done)
      throw new NoSuchElementException();
    int avail = maxLineLength - prefix.length();
    int breakPos;
    boolean tooBig = false;
    if ((model.length() - modelPos) + suffix.length() > avail) {
      if (iter == null)
        iter = new BreakIterator(model);
      breakPos = -1;
      do {
        int pos = iter.getPos();
        if (pos >= model.length())
          break;
        int w = pos - modelPos;
        if (w > 0) {
          if (w > avail) {
            if (breakPos == -1) {
              breakPos = pos;
              tooBig = true;
            }
            break;
          }
          breakPos = pos;
        }
      } while (iter.advance());
      if (breakPos == -1) {
        tooBig = true;
        breakPos = model.length();
      }
    }
    else
      breakPos = model.length();
    int nextModelPos;
    if (breakPos < model.length() && model.charAt(breakPos) == ' ')
      nextModelPos = breakPos + 1;
    else
      nextModelPos = breakPos;
    StringBuffer buf = new StringBuffer();
    if (modelPos == 0)
      buf.append(prefix);
    else {
      for (int i = 0, len = prefix.length(); i < len; i++)
        buf.append(' ');
    }
    if (tooBig && (modelPos != 0 || breakPos != model.length())) {
      String nestSuffix;
      if (breakPos == model.length()) {
        done = true;
        nestSuffix = suffix;
      }
      else
        nestSuffix = "";
      nested = new ModelBreaker(buf.toString(), model.substring(modelPos, breakPos), nestSuffix, maxLineLength);
      modelPos = nextModelPos;
      return nested.nextLine();
    }
    buf.append(model.substring(modelPos, breakPos));
    if (nextModelPos == model.length()) {
      done = true;
      buf.append(suffix);
    }
    modelPos = nextModelPos;
    return buf.toString();
  }

  private static boolean isSingleGroup(String model) {
    int length = model.length();
    if (length == 0)
      return false;
    int i = 0;
    if (model.charAt(0) == '|')
      i++;
    if (model.charAt(i) != '(')
      return false;
    loop:
    while (length > i) {
      switch (model.charAt(length - 1)) {
      case '*': case '+': case '?': case ',': case '|': case ')':
        length--;
        break;
      default:
        break loop;
      }
    }
    int level = 0;
    for (++i; i < length; i++)
      switch (model.charAt(i)) {
      case '(':
        level++;
        break;
      case ')':
        if (level == 0)
          return false;
        level--;
        break;
      }
    return true;
  }

  static public void main(String[] args) throws NumberFormatException {
    for (ModelBreaker breaker = new ModelBreaker(args[0], args[1], args[2], Integer.parseInt(args[3]));
         breaker.hasNextLine();)
      System.err.println(breaker.nextLine());
  }
}
