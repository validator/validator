package com.thaiopensource.relaxng.util;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.Parser;
import org.xml.sax.helpers.ParserFactory;
import org.xml.sax.helpers.ParserAdapter;

import com.thaiopensource.relaxng.XMLReaderCreator;

/**
 * An <code>XMLReaderCreator</code> that creates <code>XMLReader</code>s using the SAX1 <code>ParserFactory</code>.
 * An instance of this class is safe for concurrent access by multiple threads.
 *
 * @see ParserFactory
 * @see ParserAdapter
 * @author <a href="mailto:jjc@jclark.com">James Clark</a>
 */
public class Sax1XMLReaderCreator implements XMLReaderCreator {
  private final String className;

  /**
   * Constructs a <code>Sax1XMLReaderCreator</code> that uses system defaults to construct <code>XMLReader</code>s.
   */
  public Sax1XMLReaderCreator() {
    this.className = null;
  }

 /**
  * Constructs a <code>Sax1XMLReaderCreator</code> that constructs <code>XMLReader</code>s from a
  * <code>Parser</code> with the specified class name.
  *
  * @param className the fully-qualified name of the class implementing <code>Parser</code>;
  * if <code>null</code> equivalent to the no-argument constructor
  *
  */
  public Sax1XMLReaderCreator(String className) {
    this.className = className;
  }

  public XMLReader createXMLReader() throws SAXException {
    try {
      Parser parser;
      if (className == null)
        parser = ParserFactory.makeParser();
      else
        parser = ParserFactory.makeParser(className);
      return new ParserAdapter(parser);
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
