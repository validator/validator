package com.thaiopensource.relaxng.output.rnc;

import java.io.Writer;
import java.io.IOException;
import java.util.Stack;

public class StreamingPrettyprinter implements Prettyprinter {
  private final String lineSep;
  private final Writer w;

  static private class Group {
    /**
     * Serial number of the segment containing the close of the group
     * or -1 if the segment is not yet closed.
     */
    int closeSegmentSerial = -1;
    final int level;
    boolean broken = false;
    Group unbrokenParent;
    Group(Group parent) {
      this.level = parent == null ? 0 : parent.level + 1;
      if (parent != null && !parent.broken)
        unbrokenParent = parent;
    }

    void setBroken() {
      broken = true;
      if (unbrokenParent != null) {
        unbrokenParent.setBroken();
        unbrokenParent = null;
      }
    }
  }

  static private class Segment {
    /**
     * Reference to the next segment.
     */
    Segment next;
    /**
     * The characters in the segment including the characters from the terminating soft newline
     */
    StringBuffer buf = new StringBuffer();
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
    Segment(int serial) {
      this.serial = serial;
    }
  }

  private Segment head;
  private Segment tail;
  private int nextSegmentSerial = 0;
  private Group currentGroup = null;
  private Stack groupStack = new Stack();
  private int currentIndent = 0;
  private Stack indentStack = new Stack();
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
  private int maxWidth = 40;

  private Group noBreakGroup = null;

  public StreamingPrettyprinter(String lineSep, Writer w, String encoding) {
    this.lineSep = lineSep;
    this.w = w;
    head = makeSegment();
    tail = head;
    availWidth = maxWidth;
  }

  private Segment makeSegment() {
    return new Segment(nextSegmentSerial++);
  }

  public void startGroup() {
    if (currentGroup != null)
      groupStack.push(currentGroup);
    currentGroup = new Group(currentGroup);
  }

  public void endGroup() {
    if (noBreakGroup == currentGroup)
      noBreakGroup = null;
    currentGroup.closeSegmentSerial = tail.serial;
    if (groupStack.isEmpty())
      currentGroup = null;
    else
      currentGroup = (Group)groupStack.pop();
  }

  public void startNest(String indent) {
    indentStack.push(new Integer(currentIndent));
    currentIndent += indent.length();
  }

  public void endNest() {
    currentIndent = ((Integer)indentStack.pop()).intValue();
  }

  public void text(String str) {
    tail.buf.append(str);
    totalWidth += str.length();
    tryFlush(false);
  }

  public void softNewline(String noBreak) {
    if (head == tail || noBreakGroup == null)
      lastPossibleBreak = tail;
    tail.buf.append(noBreak);
    tail.preBreakDiscardCount = noBreak.length();
    if (currentGroup != null) {
      tail.group = currentGroup;
      if (noBreakGroup == null)
        noBreakGroup = currentGroup;
    }
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
    if (currentGroup != null) {
      tail.group = currentGroup;
      if (noBreakGroup == null)
        noBreakGroup = currentGroup;
    }
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
    if (lastPossibleBreak.group != null && lastPossibleBreak.group.broken)
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
      lastPossibleBreak.buf.setLength(lastPossibleBreak.buf.length() - lastPossibleBreak.preBreakDiscardCount);
      for (; s != null; s = s.next)
        write(s.buf.toString());
      writeNewline(lastPossibleBreak.indent);
      availWidth = maxWidth - lastPossibleBreak.indent;
      if (lastPossibleBreak.group != null)
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
      totalWidth += s.buf.length();
      if (lastPossibleBreak == null
          || (nbg == null
              && (lastPossibleBreak.group == null || !lastPossibleBreak.group.broken)))
        lastPossibleBreak = s;
      if (nbg == null)
        nbg = s.group;
    }
    totalWidth += tail.buf.length();
    if (nbg != null && nbg.closeSegmentSerial == -1)
      noBreakGroup = nbg;
    else
      noBreakGroup = null;
  }

  public void close() {
    if (tail.buf.length() != 0) {
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
      throw new WrappedException(e);
    }
  }

  private void write(String str) {
    try {
      w.write(str);
    }
    catch (IOException e) {
      throw new WrappedException(e);
    }
  }

  private void writeNewline(int indent) {
    try {
      w.write(lineSep);
      for (int i = 0; i < indent; i++)
        w.write(' ');
    }
    catch (IOException e) {
      throw new WrappedException(e);
    }
  }
}
