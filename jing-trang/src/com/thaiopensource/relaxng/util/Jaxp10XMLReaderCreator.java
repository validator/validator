package com.thaiopensource.relaxng.util;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.helpers.ParserAdapter;
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;

import com.thaiopensource.relaxng.XMLReaderCreator;

public class Jaxp10XMLReaderCreator implements XMLReaderCreator {
    
  private SAXParserFactory factory;

  public Jaxp10XMLReaderCreator() {
    factory = SAXParserFactory.newInstance();
    factory.setNamespaceAware(false);
    factory.setValidating(false);
  }

  public XMLReader createXMLReader() throws SAXException {
    try {
      return new ParserAdapter(factory.newSAXParser().getParser());
    }
    catch (ParserConfigurationException e) {
      throw new SAXException(e);
    }
  }
}
