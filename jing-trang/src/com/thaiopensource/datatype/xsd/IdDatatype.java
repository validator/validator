package com.thaiopensource.datatype.xsd;

import org.xml.sax.SAXException;
import org.xml.sax.Locator;

import com.thaiopensource.datatype.DatatypeContext;

class IdDatatype extends NCNameDatatype implements AssignmentClass {
  public void assign(DatatypeAssignmentImpl a, String value, DatatypeContext dc, Locator loc)
    throws SAXException {
    a.assignId(normalizeWhiteSpace(value), loc);
  }

  public Object getAssignmentClass() {
    return this;
  }
}
