package com.thaiopensource.relaxng.output.rnc;

import java.io.Writer;
import java.io.IOException;

public class StreamingPrettyprinter implements Prettyprinter {
  private final String lineSep;
  private final Writer w;

  static private class Group {
    /**
     * Serial number of the segment containing the close of the group
     * or -1 if the segment is not yet closed.
     */
    int closeSegmentSerial = -1;
    boolean broken;
    final Group parent;
    Group(Group parent) {
      this.parent = parent;
      broken = (parent == null);
    }

    void setBroken() {
      if (!broken) {
        broken = true;
        parent.setBroken();
      }
    }
  }

  static private class Segment {
    /**
     * Reference to the next segment.
     */
    Segment next;
    /**
     * Number of characters to discard at the end of the segment if we break immediately after
     * the segment
     */
    int preBreakDiscardCount = -1;
    /**
     * The current group at the end of the segment.
     */
    Group group = null;
    /**
     * The indent to be written after the newline at the end of this segment
     */
    int indent;
    final int serial;
    private static final int ALLOC_SPARE = 5;
    private static final int ALLOC_INIT = 10;
    /**
     * The characters in the segment including the characters from the terminating soft newline
     */
    char[] chars = new char[ALLOC_INIT];
    /**
     * The number of characters in chars
     */
    int length = 0;
    Segment(int serial) {
      this.serial = serial;
    }

    void append(String str) {
      if (str.length() > chars.length - length) {
        int newSize = chars.length * 2;
        if (newSize - length < str.length())
          newSize = chars.length + str.length() + ALLOC_SPARE;
        char[] newChars = new char[newSize];
        System.arraycopy(chars, 0, newChars, 0, length);
        chars = newChars;
      }
      str.getChars(0, str.length(), chars, length);
      length += str.length();
    }
  }

  private Segment head;
  private Segment tail;
  private int nextSegmentSerial = 0;
  private Group currentGroup = new Group(null);
  private int currentIndent = 0;
  private int[] indentStack = new int[10];
  private int indentLevel = 0;
  /**
   * The total width of the segments between head and tail inclusive.
   */
  private int totalWidth = 0;
  /**
   * Available width on this line.
   */
  private int availWidth;
  /**
   * The last possible breakpoint discovered on the line is immediately after this
   * segment.
   */
  private Segment lastPossibleBreak = null;

  /**
   * Maximum allowable line width (not including newline char).
   */
  private final int maxWidth;

  private Group noBreakGroup = null;

  public StreamingPrettyprinter(int maxWidth, String lineSep, Writer w) {
    this.lineSep = lineSep;
    this.w = w;
    this.maxWidth = maxWidth;
    this.availWidth = maxWidth;
    head = makeSegment();
    tail = head;
  }

  private Segment makeSegment() {
    return new Segment(nextSegmentSerial++);
  }

  public void startGroup() {
    currentGroup = new Group(currentGroup);
  }

  public void endGroup() {
    if (noBreakGroup == currentGroup)
      noBreakGroup = null;
    currentGroup.closeSegmentSerial = tail.serial;
    currentGroup = currentGroup.parent;
  }

  public void startNest(String indent) {
    if (indentLevel >= indentStack.length) {
      int[] newStack = new int[indentStack.length * 2];
      System.arraycopy(indentStack, 0, newStack, 0, indentStack.length);
      indentStack = newStack;
    }
    indentStack[indentLevel++] = currentIndent;
    currentIndent += indent.length();
  }

  public void endNest() {
    currentIndent = indentStack[--indentLevel];
  }

  public void text(String str) {
    tail.append(str);
    totalWidth += str.length();
    tryFlush(false);
  }

  public void softNewline(String noBreak) {
    if (head == tail || noBreakGroup == null)
      lastPossibleBreak = tail;
    tail.append(noBreak);
    tail.preBreakDiscardCount = noBreak.length();
    tail.group = currentGroup;
    if (noBreakGroup == null)
      noBreakGroup = currentGroup;
    tail.indent = currentIndent;
    totalWidth += tail.preBreakDiscardCount;
    Segment tem = makeSegment();
    tail.next = tem;
    tail = tem;
    tryFlush(false);
  }

  public void hardNewline() {
    if (head == tail || noBreakGroup == null)
      lastPossibleBreak = tail;
    tail.preBreakDiscardCount = 0;
    tail.group = currentGroup;
    if (noBreakGroup == null)
      noBreakGroup = currentGroup;
    tail.indent = currentIndent;
    Segment tem = makeSegment();
    tail.next = tem;
    tail = tem;
    tryFlush(true);
  }

  private boolean shouldKeepLooking(boolean hard) {
    if (lastPossibleBreak == null)
      return true;
    if (hard)
      return false;
    if (totalWidth > availWidth)
      return false;
    if (lastPossibleBreak.group.broken)
      return false;
    return true;
  }

  private void tryFlush(boolean hard) {
    for (;;) {
      if (shouldKeepLooking(hard))
        return;
      Segment s = head;
      head = lastPossibleBreak.next;
      lastPossibleBreak.next = null;
      lastPossibleBreak.length -= lastPossibleBreak.preBreakDiscardCount;
      for (; s != null; s = s.next)
        write(s.chars, 0, s.length);
      writeNewline(lastPossibleBreak.indent);
      availWidth = maxWidth - lastPossibleBreak.indent;
      lastPossibleBreak.group.setBroken();
      update();
    }
  }


  private void update() {
    lastPossibleBreak = null;
    totalWidth = 0;
    Group nbg = null;
    for (Segment s = head; s != tail; s = s.next) {
      if (nbg != null
          && nbg.closeSegmentSerial != -1
          && s.serial >= nbg.closeSegmentSerial)
        nbg = null;
      totalWidth += s.length;
      if (lastPossibleBreak == null
          || (!lastPossibleBreak.group.broken && nbg == null))
        lastPossibleBreak = s;
      if (nbg == null)
        nbg = s.group;
    }
    totalWidth += tail.length;
    if (nbg != null && nbg.closeSegmentSerial == -1)
      noBreakGroup = nbg;
    else
      noBreakGroup = null;
  }

  public void close() {
    if (tail.length != 0) {
      // Don't want spaces after final newline
      currentIndent = 0;
      hardNewline();
    }
    else if (head != tail) {
      // Avoid spaces after final newline
      for (Segment s = head;; s = s.next) {
        if (s.next == tail) {
          s.indent = 0;
          break;
        }
      }
      tryFlush(true);
    }
    try {
      w.close();
    }
    catch (IOException e) {
      throw new Prettyprinter.WrappedException(e);
    }
  }

  private void write(char[] chars, int off, int len) {
    try {
      w.write(chars, off, len);
    }
    catch (IOException e) {
      throw new Prettyprinter.WrappedException(e);
    }
  }

  private void writeNewline(int indent) {
    try {
      w.write(lineSep);
      for (int i = 0; i < indent; i++)
        w.write(' ');
    }
    catch (IOException e) {
      throw new Prettyprinter.WrappedException(e);
    }
  }
}
