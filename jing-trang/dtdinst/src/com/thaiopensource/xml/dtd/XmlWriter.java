package com.thaiopensource.xml.dtd;

import java.io.Writer;
import java.io.IOException;

public class XmlWriter {
  static final private String indentString = "  ";
  private final Writer writer;

  private static final int OTHER = 0; // must be at beginning of line
  private static final int IN_START_TAG = 1;
  private static final int AFTER_DATA = 2;
  private int state = OTHER;

  private boolean startTagOpen = false;
  private String[] stack = new String[20];
  private int level = 0;
  private String newline = "\n";
  private boolean inData = false;

  public XmlWriter(Writer writer) {
    this.writer = writer;
  }

  public void startElement(String name) throws IOException {
    switch (state) {
    case IN_START_TAG:
      writer.write('>');
      writer.write(newline);
      indent();
      break;
    case OTHER:
      indent();
      // fall through
    case AFTER_DATA:
      state = IN_START_TAG;
      break;
    }
    writer.write('<');
    writer.write(name);
    push(name);
  }

  public void endElement() throws IOException {
    String name = pop();
    switch (state) {
    case IN_START_TAG:
      writer.write("/>");
      break;
    case OTHER:
      indent();
      // fall through
    case AFTER_DATA:
      writer.write("</");
      writer.write(name);
      writer.write('>');
      break;
    }
    writer.write(newline);
    state = OTHER;
  }

  public void attribute(String name, String value) throws IOException {
    if (state != IN_START_TAG)
      throw new IllegalStateException();
    writer.write(' ');
    writer.write(name);
    writer.write('=');
    writer.write('"');
    outputData(value);
    writer.write('"');
  }

  public void characters(String str) throws IOException {
    if (state == IN_START_TAG)
      writer.write('>');
    state = AFTER_DATA;
    outputData(str);
  }

  public void comment(String str) throws IOException {
    if (state == IN_START_TAG) {
      writer.write('>');
      state = OTHER;
      writer.write(newline);
    }
    writer.write("<!--");
    writer.write(str);
    writer.write("-->");
    if (state != AFTER_DATA)
      writer.write(newline);
  }

  private void outputData(String str) throws IOException {
    int len = str.length();
    for (int i = 0; i < len; i++) {
      char c = str.charAt(i);
      switch (c) {
      case '<':
	writer.write("&lt;");
	break;
      case '>':
	writer.write("&gt;");
	break;
      case '&':
	writer.write("&amp;");
	break;
      case '"':
	writer.write("&quot;");
	break;
      default:
	if (c >= 0x80) {
	  if (Utf16.isSurrogate1(c)) {
	    ++i;
	    if (i < len) {
	      char c2 = str.charAt(i);
	      if (Utf16.isSurrogate2(c)) {
		charRef(Utf16.scalarValue(c, c2));
		break;
	      }
	    }
	    throw new IOException("surrogate pair integrity failure");
	  }
	  charRef(c);
	}
	else
	  writer.write(c);
	break;
      }
    }
  }

  private void charRef(int c) throws IOException {
    writer.write("&#x");
    int nDigits = c > 0xFFFF ? 6 : 4;
    for (int i = 0; i < nDigits; i++)
      writer.write("0123456789ABCDEF".charAt((c >> (4*(nDigits - 1 - i)))
					     & 0xF));
    writer.write(";");
  }
    
  private void indent() throws IOException {
    for (int i = 0; i < level; i++)
      writer.write(indentString);
  }

  private final void push(String name) {
    if (level == stack.length) {
      String[] tem = stack;
      stack = new String[stack.length * 2];
      System.arraycopy(tem, 0, stack, 0, tem.length);
    }
    stack[level++] = name;
  }
  
  private final String pop() {
    return stack[--level];
  }
}
