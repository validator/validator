package com.thaiopensource.relaxng.util;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.ParserFactory;
import org.xml.sax.helpers.ParserAdapter;

import com.thaiopensource.relaxng.XMLReaderCreator;

public class XMLReaderCreatorImpl1 implements XMLReaderCreator {
  private String className;

  public XMLReaderCreatorImpl1(String className) {
    this.className = className;
  }

  public XMLReader createXMLReader() throws SAXException {
    try {
      return new ParserAdapter(ParserFactory.makeParser(className));
    }
    catch (ClassNotFoundException e) {
      throw new SAXException(e);
    }
    catch (InstantiationException e) {
      throw new SAXException(e);
    }
    catch (IllegalAccessException e) {
      throw new SAXException(e);
    }
  }
}
