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

  private int widthMax = 60;

  abstract class OutputItem {
    abstract int unbrokenWidth();
    abstract void output(boolean broken);
  }

  class GroupItem extends OutputItem {
    private final List members = new Vector();
    private int width = UNKNOWN;

    void output(boolean broken) {
      outputItems(members, broken && unbrokenWidth() > widthMax - outputPos);
    }

    int unbrokenWidth() {
      if (width == UNKNOWN) {
        int w = 0;
        for (Iterator iter = members.iterator(); iter.hasNext() && w != IMPOSSIBLE;) {
          int n = ((OutputItem)iter.next()).unbrokenWidth();
          if (n == IMPOSSIBLE)
            w = IMPOSSIBLE;
          else if (n > Integer.MAX_VALUE - w)
            w = IMPOSSIBLE;
          else
            w += n;
        }
        width = w;
      }
      return width;
    }
  }

  class TextItem extends OutputItem {
    private final String str;
    TextItem(String str) {
      this.str = str;
    }

    int unbrokenWidth() {
      return str.length();
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
  }

  class HardNewlineItem extends OutputItem {
    private final int indent;

    HardNewlineItem(int indent) {
      this.indent = indent;
    }

    int unbrokenWidth() {
      return IMPOSSIBLE;
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


  private void outputItems(List list, boolean broken) {
    for (Iterator iter = list.iterator(); iter.hasNext();)
      ((OutputItem)iter.next()).output(broken);
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
    outputItems(outputList, true);
    try {
      w.close();
    }
    catch (IOException e) {
      throw new WrappedException(e);
    }
  }
}
