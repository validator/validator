package com.thaiopensource.datatype.xsd;

import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;

import com.thaiopensource.datatype.DatatypeAssignment;
import com.thaiopensource.datatype.DatatypeContext;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.Locator;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.LocatorImpl;

class DatatypeAssignmentImpl implements DatatypeAssignment {
  private XMLReader xr;

  static class Id {
    boolean defined;
    Locator defLoc;
    Vector refLocs;
  }

  private Hashtable idTable = new Hashtable();

  DatatypeAssignmentImpl(XMLReader xr) {
    this.xr = xr;
  }

  public void assign(String value, Object cls, DatatypeContext dc, Locator loc) throws SAXException {
    ((AssignmentClass)cls).assign(this, value, dc, loc);
  }

  public void end() throws SAXException {
    for (Enumeration e = idTable.keys(); e.hasMoreElements();) {
      String str = (String)e.nextElement();
      Id id = (Id)idTable.get(str);
      if (!id.defined) {
	for (int i = 0; i < id.refLocs.size(); i++)
	  error("undefined_idref",
		str,
		(Locator)id.refLocs.elementAt(i));
      }
    }
  }
  
  void assignId(String value, Locator loc) throws SAXException {
    Id id = lookupId(value);
    if (id.defined)
      error("duplicate_id", value, loc);
    else {
      id.defined = true;
      id.defLoc = saveLocator(loc);
    }
    id.refLocs = null;
  }

  void assignIdref(String value, Locator loc) throws SAXException {
    Id id = lookupId(value);
    if (id.defined)
      return;
    if (id.refLocs == null)
      id.refLocs = new Vector();
    id.refLocs.addElement(saveLocator(loc));
  }

  private Id lookupId(String value) {
    Id id = (Id)idTable.get(value);
    if (id == null) {
      id = new Id();
      idTable.put(value, id);
    }
    return id;
  }

  static private Locator saveLocator(Locator loc) {
    if (loc == null)
      return new LocatorImpl();
    return new LocatorImpl(loc);
  }

  private void error(String key, String arg, Locator loc) throws SAXException {
    ErrorHandler eh = xr.getErrorHandler();
    if (eh != null)
      eh.error(new SAXParseException(Localizer.message(key, arg), loc));
  }
}
