package com.thaiopensource.relaxng.output.rnc;

import java.io.IOException;
import java.io.Writer;
import java.util.Stack;
import java.util.List;
import java.util.Vector;
import java.util.Iterator;

public class PrettyprinterImpl implements Prettyprinter {
  private final String lineSep;
  private final Writer w;
  private int currentIndent = 0;
  private Stack indentStack = new Stack();
  private int outputPos = 0;
  private List outputList = new Vector();
  private Stack outputListStack = new Stack();

  private final int IMPOSSIBLE = Integer.MAX_VALUE;
  private final int UNKNOWN = -1;

  private int widthMax = 40;

  abstract class OutputItem {
    abstract int getUnbrokenWidth();
    abstract int getPreBreakWidth();
    abstract void output(boolean broken);
  }

  class GroupItem extends OutputItem {
    private final List members = new Vector();
    private int unbrokenWidth = UNKNOWN;
    private int preBreakWidth = UNKNOWN;

    void output(boolean broken) {
      if (broken)
        outputBroken(members);
      else
        outputUnbroken(members);
    }

    int getUnbrokenWidth() {
      if (unbrokenWidth == UNKNOWN) {
        int w = 0;
        for (Iterator iter = members.iterator(); iter.hasNext() && w != IMPOSSIBLE;) {
          int n = ((OutputItem)iter.next()).getUnbrokenWidth();
          if (n == IMPOSSIBLE)
            w = IMPOSSIBLE;
          else if (n > Integer.MAX_VALUE - w)
            w = IMPOSSIBLE;
          else
            w += n;
        }
        unbrokenWidth = w;
      }
      return unbrokenWidth;
    }

    int getPreBreakWidth() {
      if (preBreakWidth == UNKNOWN) {
        int w = 0;
        for (Iterator iter = members.iterator(); iter.hasNext();) {
          OutputItem item = (OutputItem)iter.next();
          int n = item.getPreBreakWidth();
          if (n != IMPOSSIBLE)
            return preBreakWidth = w + n;
          w += item.getUnbrokenWidth();
        }
        return IMPOSSIBLE;
      }
      return preBreakWidth;
    }
  }

  class TextItem extends OutputItem {
    private final String str;
    TextItem(String str) {
      this.str = str;
    }

    int getUnbrokenWidth() {
      return str.length();
    }

    int getPreBreakWidth() {
      return IMPOSSIBLE;
    }

    void output(boolean broken) {
      write(str);
    }
  }

  class SoftNewlineItem extends TextItem {
    private final int indent;
    SoftNewlineItem(String str, int indent) {
      super(str);
      this.indent = indent;
    }

    void output(boolean broken) {
      if (broken)
        writeNewline(indent);
      else
        super.output(broken);
    }

    int getPreBreakWidth() {
      return 0;
    }
  }

  class HardNewlineItem extends OutputItem {
    private final int indent;

    HardNewlineItem(int indent) {
      this.indent = indent;
    }

    int getUnbrokenWidth() {
      return IMPOSSIBLE;
    }

    int getPreBreakWidth() {
      return 0;
    }

    void output(boolean broken) {
      writeNewline(indent);
    }
  }

  public static class WrappedException extends RuntimeException {
    private IOException cause;

    public Throwable getCause() {
      return cause;
    }

    public IOException getIOException() {
      return cause;
    }

    private WrappedException(IOException cause) {
      this.cause = cause;
    }
  }

  public PrettyprinterImpl(String lineSep, Writer w, String encoding) {
    this.lineSep = lineSep;
    this.w = w;
  }

  public void hardNewline() {
    outputList.add(new HardNewlineItem(currentIndent));
  }

  public void softNewline(String noBreak) {
    outputList.add(new SoftNewlineItem(noBreak, currentIndent));
  }

  public void text(String str) {
    outputList.add(new TextItem(str));
  }

  public void startNest(String indent) {
    indentStack.push(new Integer(currentIndent));
    currentIndent += indent.length();
  }

  public void endNest() {
    currentIndent = ((Integer)indentStack.pop()).intValue();
  }

  public void startGroup() {
    GroupItem g = new GroupItem();
    outputList.add(g);
    outputListStack.push(outputList);
    outputList = g.members;
  }

  public void endGroup() {
    outputList = (List)outputListStack.pop();
  }


  private void outputBroken(List list) {
    for (int i = 0, len = list.size(); i < len; i++) {
      OutputItem oi = (OutputItem)list.get(i);
      if (oi instanceof GroupItem
          && canBreakWithin(list, i + 1, widthMax - outputPos - oi.getUnbrokenWidth()))
        oi.output(false);
      else
        oi.output(true);
    }
  }

  private boolean canBreakWithin(List list, int i, int avail) {
    for (int len = list.size(); avail >= 0 && i < len; i++) {
      OutputItem oi = (OutputItem)list.get(i);
      int n = oi.getPreBreakWidth();
      if (n <= avail)
        return true;
      avail -= oi.getUnbrokenWidth();
    }
    return false;
  }

  private void outputUnbroken(List list) {
    for (Iterator iter = list.iterator(); iter.hasNext();)
      ((OutputItem)iter.next()).output(false);
  }

  private void write(String str) {
    try {
      w.write(str);
      outputPos += str.length();
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
      outputPos = indent;
    }
    catch (IOException e) {
      throw new WrappedException(e);
    }
  }

  public void close() {
    outputBroken(outputList);
    try {
      w.close();
    }
    catch (IOException e) {
      throw new WrappedException(e);
    }
  }
}
