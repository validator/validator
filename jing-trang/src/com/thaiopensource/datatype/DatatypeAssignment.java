package com.thaiopensource.datatype;

import org.xml.sax.SAXException;
import org.xml.sax.Locator;

public interface DatatypeAssignment {
  void assign(String value, Object cls, DatatypeContext dc, Locator loc)
    throws SAXException;
  void end() throws SAXException;
}
