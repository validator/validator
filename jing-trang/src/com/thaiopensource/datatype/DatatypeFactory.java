package com.thaiopensource.datatype;

public interface DatatypeFactory {
  /**
   * Create a Datatype for the specified namespaceURI and localName.
   * Return null if this DatatypeFactory does not recognize the
   * specified namespaceURI and localName.
   */
  Datatype createDatatype(String namespaceURI, String localName);
}
