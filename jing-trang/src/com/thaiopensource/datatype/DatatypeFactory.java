package com.thaiopensource.datatype;

import org.xml.sax.XMLReader;

public interface DatatypeFactory {
  /**
   * Create a Datatype for the specified namespaceURI and localName.
   * Return null if this DatatypeFactory does not recognize the
   * specified namespaceURI and localName.
   */
  Datatype createDatatype(String namespaceURI, String localName);
  
  DatatypeReader createDatatypeReader(String namespaceURI, DatatypeContext context);

  DatatypeAssignment createDatatypeAssignment(XMLReader xr);
}
