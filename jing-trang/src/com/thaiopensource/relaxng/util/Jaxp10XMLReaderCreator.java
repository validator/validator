package com.thaiopensource.relaxng.util;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.helpers.ParserAdapter;
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;

import com.thaiopensource.relaxng.XMLReaderCreator;

/**
 * An <code>XMLReaderCreator</code> that creates an <code>XMLReader</code> by using JAXP 1.0.
 * It first creates a <code>Parser</code> and then wraps that with a <code>ParserAdapter</code>.
 * An instance of this class is <em>not</em> safe for concurrent access by multiple threads.
 *
 * @see SAXParserFactory
 * @see ParserAdapter
 * @author <a href="mailto:jjc@jclark.com">James Clark</a>
 */
public class Jaxp10XMLReaderCreator implements XMLReaderCreator {
    
  private final SAXParserFactory factory;

  /**
   * Default constructor.
   */
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
