package com.thaiopensource.datatype.xsd;

import org.relaxng.datatype.DatatypeLibrary;
import org.relaxng.datatype.DatatypeLibraryFactory;

public class DatatypeLibraryFactoryImpl implements DatatypeLibraryFactory {
  static private final String xsdns
    = "http://www.w3.org/2001/XMLSchema-datatypes";

  public DatatypeLibrary createDatatypeLibrary(String uri) {
    if (!xsdns.equals(uri))
      return null;
    return new DatatypeLibraryImpl();
  }
}
