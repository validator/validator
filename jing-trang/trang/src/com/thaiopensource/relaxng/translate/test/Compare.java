package com.thaiopensource.relaxng.translate.test;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.List;
import java.util.Vector;
import java.util.Collections;
import java.util.Comparator;
import java.io.IOException;
import java.io.File;

import com.thaiopensource.util.UriOrFile;
import com.thaiopensource.xml.sax.XMLReaderCreator;
import com.thaiopensource.xml.sax.AbstractLexicalHandler;
import com.thaiopensource.xml.sax.Jaxp11XMLReaderCreator;

public class Compare {
  static public boolean compare(File file1, File file2, XMLReaderCreator xrc) throws SAXException, IOException {
    return load(xrc, file1).equals(load(xrc, file2));
  }

  static private List load(XMLReaderCreator xrc, File file) throws SAXException, IOException {
    InputSource in = new InputSource(UriOrFile.fileToUri(file));
    Saver saver = new Saver();
    XMLReader xr = xrc.createXMLReader();

    try {
      xr.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
    }
    catch (SAXNotRecognizedException e) {
      throw new SAXException("support for namespaces-prefixes feature required");
    }
    catch (SAXNotSupportedException e) {
      throw new SAXException("support for namespaces-prefixes feature required");
    }
    xr.setContentHandler(saver);
    try {
      xr.setProperty("http://xml.org/sax/properties/lexical-handler", new CommentSaver(saver));
    }
    catch (SAXNotRecognizedException e) {
    }
    catch (SAXNotSupportedException e) {
    }
    xr.parse(in);
    return saver.getEventList();
  }

  static abstract class Event {
    boolean merge(char[] chars, int start, int count) {
      return false;
    }
    boolean isWhitespace() {
      return false;
    }
  }

  static class StartElement extends Event {
    private final String qName;

    StartElement(String qName) {
      this.qName = qName;
    }

    public boolean equals(Object obj) {
      if (!(obj instanceof StartElement))
        return false;
      return qName.equals(((StartElement)obj).qName);
    }
  }

  static class Attribute extends Event {
    private final String qName;
    private final String value;

    Attribute(String qName, String value) {
      this.qName = qName;
      this.value = value;
    }

    String getQName() {
      return qName;
    }

    public boolean equals(Object obj) {
      if (!(obj instanceof Attribute))
        return false;
      Attribute other = (Attribute)obj;
      return qName.equals(other.qName) && value.equals(other.value);
    }
  }

  static class EndElement extends Event {
    public boolean equals(Object obj) {
      return obj instanceof EndElement;
    }
  }

  static class Comment extends Event {
    private final String value;

    Comment(String value) {
      this.value = value;
    }

    public boolean equals(Object obj) {
      if (!(obj instanceof Comment))
        return false;
      return value.equals(((Comment)obj).value);
    }
  }

  static class Text extends Event {
    private String value;

    Text(String value) {
      this.value = value;
    }

    public boolean equals(Object obj) {
      if (!(obj instanceof Text))
        return false;
      return value.equals(((Text)obj).value);
    }

    boolean isWhitespace() {
      for (int i = 0, len = value.length(); i < len; i++) {
        switch (value.charAt(i)) {
        case '\r':
        case '\n':
        case '\t':
        case ' ':
          break;
        default:
          return false;
        }
      }
      return true;
    }

    boolean merge(char[] chars, int start, int count) {
      StringBuffer buf = new StringBuffer(value);
      buf.append(chars, start, count);
      value = buf.toString();
      return true;
    }
  }

  static class Saver extends DefaultHandler {
    private final List eventList = new Vector();
    private final List attributeList = new Vector();

    List getEventList() {
      return eventList;
    }

    void flushWhitespace(boolean endElement) {
      int len = eventList.size();
      if (len == 0)
        return;
      if (!((Event)eventList.get(len - 1)).isWhitespace())
        return;
      if (endElement && len > 1 && eventList.get(len - 2) instanceof StartElement)
        return;
      eventList.remove(len - 1);
    }

    public void startElement(String ns, String localName, String qName, Attributes attributes) {
      flushWhitespace(false);
      eventList.add(new StartElement(qName));
      for (int i = 0, len = attributes.getLength(); i < len; i++)
        attributeList.add(new Attribute(attributes.getQName(i), attributes.getValue(i)));
      Collections.sort(attributeList, new Comparator() {
        public int compare(Object o1, Object o2) {
          return ((Attribute)o1).getQName().compareTo(((Attribute)o2).getQName());
        }
      });
      eventList.addAll(attributeList);
      attributeList.clear();
    }

    public void endElement(String ns, String localName, String qName) {
      flushWhitespace(true);
      eventList.add(new EndElement());
    }

    public void characters(char[] chars, int start, int length) {
      int len = eventList.size();
      if (len == 0 || !((Event)eventList.get(len - 1)).merge(chars, start, length))
        eventList.add(new Text(new String(chars, start, length)));
    }

    public void ignorableWhitespace(char[] chars, int start, int length) {
      characters(chars, start, length);
    }

    public void endDocument() {
      flushWhitespace(false);
    }

    void comment(String value) {
      flushWhitespace(false);
      eventList.add(new Comment(value));
    }
  }

  static class CommentSaver extends AbstractLexicalHandler {
    private final Saver saver;
    CommentSaver(Saver saver) {
      this.saver = saver;
    }

    public void comment(char[] chars, int start, int length) throws SAXException {
      saver.comment(new String(chars, start, length));
    }
  }

  static public void main(String[] args) throws SAXException, IOException {
    System.err.println(compare(new File(args[0]), new File(args[1]), new Jaxp11XMLReaderCreator()));
  }
}
