package com.thaiopensource.relaxng.output.common;

import java.io.Writer;
import java.io.IOException;
import java.util.Stack;

public class XmlWriter {
  private String lineSep;
  private Writer w;
  private Stack tagStack = new Stack();
  private boolean inStartTag = false;
  private boolean inText = false;
  private int level = 0;
  private String[] topLevelAttributes;

  public class WrappedException extends RuntimeException {
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

  public XmlWriter(String lineSep, Writer w, String[] topLevelAttributes, String encoding) {
    this.lineSep = lineSep;
    this.w = w;
    this.topLevelAttributes = topLevelAttributes;
    write("<?xml version=\"1.0\" encoding=\"");
    write(encoding);
    write("\"?>");
    newline();
  }

  public void startElement(String name) {
    if (inStartTag) {
      maybeWriteTopLevelAttributes();
      inStartTag = false;
      write(">");
      newline();
    }
    if (inText)
      inText = false;
    else
      indent();
    write('<');
    write(name);
    tagStack.push(name);
    inStartTag = true;
    level++;
  }

  public void endElement() {
    if (inStartTag) {
      maybeWriteTopLevelAttributes();
      level--;
      inStartTag = false;
      tagStack.pop();
      write("/>");
    }
    else {
      level--;
      if (inText)
        inText = false;
      else
        indent();
      write("</");
      write((String)tagStack.pop());
      write(">");
    }
    newline();
  }

  public void attribute(String name, String value) {
    if (!inStartTag)
      throw new IllegalStateException("attribute outside of start-tag");
    write(' ');
    write(name);
    write('=');
    write('"');
    data(value);
    write('"');
  }

  public void text(String s) {
    if (s.length() == 0)
      return;
    if (inStartTag) {
      maybeWriteTopLevelAttributes();
      inStartTag = false;
      write(">");
    }
    data(s);
    inText = true;
  }

  public void comment(String s) {
    if (inStartTag) {
      maybeWriteTopLevelAttributes();
      inStartTag = false;
      write(">");
      newline();
    }
    if (!inText)
      indent();
    write("<!--");
    int start = 0;
    level++;
    for (;;) {
      int i = s.indexOf('\n', start);
      if (i < 0) {
        if (start > 0) {
          newline();
          indent();
          write(s.substring(start));
          level--;
          newline();
          indent();
        }
        else {
          level--;
          if (s.length() != 0) {
            write(' ');
            write(s);
            if (s.charAt(s.length() - 1) != ' ')
              write(' ');
          }
        }
        break;
      }
      newline();
      indent();
      write(s.substring(start, i));
      start = i + 1;
    }
    write("-->");
    if (!inText)
      newline();
  }

  public void data(String s) {
    int n = s.length();
    for (int i = 0; i < n; i++) {
      switch (s.charAt(i)) {
      case '<':
        write("&lt;");
        break;
      case '>':
        write("&gt;");
        break;
      case '&':
        write("&amp;");
        break;
      case '\r':
        write("&#xD;");
        break;
      case '\n':
        write(lineSep);
        break;
      default:
        write(s.charAt(i));
        break;
      }
    }
  }

  private void indent() {
    for (int i = 0; i < level; i++)
      write("  ");
  }

  private void newline() {
    write(lineSep);
  }

  private void maybeWriteTopLevelAttributes() {
    if (level != 1)
      return;
    for (int i = 0; i < topLevelAttributes.length; i += 2)
      attribute(topLevelAttributes[i], topLevelAttributes[i + 1]);
  }

  private void write(String s) {
    try {
      w.write(s);
    }
    catch (IOException e) {
      throw new WrappedException(e);
    }
  }

  private void write(char c) {
    try {
      w.write(c);
    }
    catch (IOException e) {
      throw new WrappedException(e);
    }
  }

  public void close() {
    try {
      w.close();
    }
    catch (IOException e) {
      throw new WrappedException(e);
    }
  }

}
