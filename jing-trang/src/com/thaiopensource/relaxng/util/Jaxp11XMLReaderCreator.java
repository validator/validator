package com.thaiopensource.relaxng.util;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;

import com.thaiopensource.relaxng.XMLReaderCreator;

public class Jaxp11XMLReaderCreator implements XMLReaderCreator {
    
  private SAXParserFactory factory;

  public Jaxp11XMLReaderCreator() {
    factory = SAXParserFactory.newInstance();
    factory.setNamespaceAware(true);
    factory.setValidating(false);
  }

  public XMLReader createXMLReader() throws SAXException {
    try {
      return factory.newSAXParser().getXMLReader();
    }
    catch (ParserConfigurationException e) {
      throw new SAXException(e);
    }
  }
}
