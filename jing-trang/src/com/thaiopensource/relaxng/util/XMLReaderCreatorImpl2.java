package com.thaiopensource.relaxng.util;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.helpers.XMLReaderFactory;

import com.thaiopensource.relaxng.XMLReaderCreator;

public class XMLReaderCreatorImpl2 implements XMLReaderCreator {
  private String className;

  public XMLReaderCreatorImpl2(String className) {
    this.className = className;
  }

  public XMLReader createXMLReader() throws SAXException {
    XMLReader xr = XMLReaderFactory.createXMLReader(className);
    xr.setFeature("http://xml.org/sax/features/namespaces", true);
    xr.setFeature("http://xml.org/sax/features/namespace-prefixes", false);
    try {
      xr.setFeature("http://xml.org/sax/features/validation", false);
    }
    catch (SAXNotRecognizedException e) {
    }
    catch (SAXNotSupportedException e) {
    }
    return xr;
  }
}
