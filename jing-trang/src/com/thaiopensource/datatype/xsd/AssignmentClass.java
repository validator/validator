package com.thaiopensource.datatype.xsd;

import org.xml.sax.SAXException;
import org.xml.sax.Locator;

import com.thaiopensource.datatype.DatatypeContext;

interface AssignmentClass {
  void assign(DatatypeAssignmentImpl a, String value,
	      DatatypeContext dc, Locator loc)
    throws SAXException;
}
