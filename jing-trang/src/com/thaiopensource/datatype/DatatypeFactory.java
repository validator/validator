package com.thaiopensource.datatype;

public interface DatatypeFactory {
  /**
   * Create a DatatypeBuilder for the specified namespaceURI and localName.
   * Return null if this DatatypeFactory does not recognize the
   * specified namespaceURI and localName.
   */
  DatatypeBuilder createDatatypeBuilder(String namespaceURI, String localName);
}
