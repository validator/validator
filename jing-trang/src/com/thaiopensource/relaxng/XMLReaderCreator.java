package com.thaiopensource.relaxng;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * A factory for <code>XMLReader</code>s.  Thread-safety is determined by each particular
 * implementation of this interface.
 *
 * @author <a href="mailto:jjc@jclark.com">James Clark</a>
 */
public interface XMLReaderCreator {
  /**
   * Creates a new <code>XMLReader</code>.
   *
   * @return a new <code>XMLReader</code>; never <code>null</code>
   * @throws SAXException If an <code>XMLReader</code> cannot be created for any reason
   */
  XMLReader createXMLReader() throws SAXException;
}
