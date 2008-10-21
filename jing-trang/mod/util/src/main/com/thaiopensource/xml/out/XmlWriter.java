package com.thaiopensource.xml.out;

import java.io.Writer;
import java.io.IOException;
import java.io.CharConversionException;

import com.thaiopensource.util.Utf16;

public class XmlWriter {
  static final private String indentString = "  ";
  private final Writer writer;
  private final CharRepertoire cr;

  private static final int OTHER = 0; // must be at beginning of line
  private static final int IN_START_TAG = 1;
  private static final int AFTER_DATA = 2;
  private int state = OTHER;

  private String[] stack = new String[20];
  private int level = 0;
  private String newline = "\n";

  public XmlWriter(Writer writer, CharRepertoire cr) {
    this.writer = writer;
    this.cr = cr;
  }

  public void setNewline(String newline) {
    this.newline = newline;
  }

  public void close() throws IOException {
    writer.close();
  }

  public void flush() throws IOException {
    writer.flush();
  }

  public void writeXmlDecl(String enc) throws IOException {
    writer.write("<?xml version=\"1.0\" encoding=\"");
    writer.write(enc);
    writer.write("\"?>");
    writer.write(newline);
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
    outputMarkup(name);
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
      outputMarkup(name);
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
    outputMarkup(name);
    writer.write('=');
    writer.write('"');
    outputData(value, true, false);
    writer.write('"');
  }

  public void characters(String str) throws IOException {
    characters(str, false);
  }

  public void characters(String str, boolean useCharRef) throws IOException {
    if (state == IN_START_TAG)
      writer.write('>');
    state = AFTER_DATA;
    outputData(str, false, useCharRef);
  }

  public void comment(String str) throws IOException {
    if (state == IN_START_TAG) {
      writer.write('>');
      state = OTHER;
      writer.write(newline);
    }
    writer.write("<!--");
    outputMarkup(str);
    writer.write("-->");
    if (state != AFTER_DATA)
      writer.write(newline);
  }

  public void processingInstruction(String target, String str) throws IOException {
    if (state == IN_START_TAG) {
      writer.write('>');
      state = OTHER;
      writer.write(newline);
    }
    writer.write("<?");
    outputMarkup(target);
    if (str.length() != 0) {
      writer.write(' ');
      outputMarkup(str);
    }
    writer.write("?>");
    if (state != AFTER_DATA)
      writer.write(newline);
  }

  private void outputMarkup(String str) throws IOException {
    int len = str.length();
    for (int i = 0; i < len; i++) {
      char c = str.charAt(i);
      if (Utf16.isSurrogate1(c)) {
	if (i == len || !Utf16.isSurrogate2(str.charAt(i)))
	  throw new CharConversionException("surrogate pair integrity failure");
	if (!cr.contains(c, str.charAt(i)))
	  throw new CharConversionException();
      }
      else if (!cr.contains(c))
	throw new CharConversionException();
    }
    outputLines(str);
  }

  private void outputLines(String str) throws IOException {
    while (str.length() > 0) {
      int i = str.indexOf('\n');
      if (i < 0) {
	writer.write(str);
	break;
      }
      if (i > 0)
	writer.write(str.substring(0, i));
      writer.write(newline);
      str = str.substring(i + 1);
    }
  }

  private void outputData(String str, boolean inAttribute, boolean useCharRef)
    throws IOException {
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
      case '\r':
	writer.write("&#xD;");
	break;
      case '\t':
	if (inAttribute)
	  writer.write("&#x9;");
	else
	  writer.write('\t');
	break;
      case '\n':
	if (inAttribute)
	  writer.write("&#xA;");
	else
	  writer.write(newline);
	break;
      default:
	if (Utf16.isSurrogate1(c)) {
	  ++i;
	  if (i < len) {
	    char c2 = str.charAt(i);
	    if (Utf16.isSurrogate2(c)) {
	      charRef(Utf16.scalarValue(c, c2));
	      break;
	    }
	  }
	  throw new CharConversionException("surrogate pair integrity failure");
	}
	if (cr.contains(c) && !useCharRef)
	  writer.write(c);
	else
	  charRef(c);
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
