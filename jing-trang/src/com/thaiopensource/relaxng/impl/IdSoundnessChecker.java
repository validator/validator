package com.thaiopensource.relaxng.impl;

import com.thaiopensource.relaxng.ValidatorHandler;
import org.relaxng.datatype.Datatype;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.LocatorImpl;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class IdSoundnessChecker implements ValidatorHandler {
  private final IdTypeMap idTypeMap;
  private ErrorHandler eh;
  private boolean hadError;
  private boolean complete;
  private Locator locator;
  private final Hashtable table = new Hashtable();

  private static class Entry {
    Locator idLoc;
    Vector idrefLocs;
    boolean hadId;
  }

  public IdSoundnessChecker(IdTypeMap idTypeMap, ErrorHandler eh) {
    this.idTypeMap = idTypeMap;
    this.eh = eh;
  }

  public void reset() {
    table.clear();
    locator = null;
    hadError = false;
    complete = false;
  }

  public boolean isValidSoFar() {
    return !hadError;
  }

  public boolean isComplete() {
    return complete;
  }

  public void setErrorHandler(ErrorHandler eh) {
    this.eh = eh;
  }

  public ErrorHandler getErrorHandler() {
    return eh;
  }

  public void setDocumentLocator(Locator locator) {
    this.locator = locator;
  }

  public void startDocument() throws SAXException {
  }

  public void endDocument() throws SAXException {
    for (Enumeration e = table.keys(); e.hasMoreElements();) {
      String token = (String)e.nextElement();
      Entry entry = (Entry)table.get(token);
      if (!entry.hadId) {
        for (Enumeration f = entry.idrefLocs.elements(); f.hasMoreElements();)
          error("missing_id", token, (Locator)f.nextElement());
      }
    }
    complete = true;
  }

  public void startPrefixMapping(String s, String s1) throws SAXException {
  }

  public void endPrefixMapping(String s) throws SAXException {
  }

  public void startElement(String namespaceUri, String localName, String qName, Attributes attributes)
          throws SAXException {
    Name elementName = new Name(namespaceUri, localName);
    int len = attributes.getLength();
    for (int i = 0; i < len; i++) {
      Name attributeName = new Name(attributes.getURI(i), attributes.getLocalName(i));
      int idType = idTypeMap.getIdType(elementName, attributeName);
      if (idType != Datatype.ID_TYPE_NULL) {
        String[] tokens = split(attributes.getValue(i));
        switch (idType) {
        case Datatype.ID_TYPE_ID:
          if (tokens.length == 1)
            id(tokens[0]);
          else if (tokens.length == 0)
            error("id_no_tokens");
          else
            error("id_multiple_tokens");
          break;
        case Datatype.ID_TYPE_IDREF:
          if (tokens.length == 1)
            idref(tokens[0]);
          else if (tokens.length == 0)
            error("idref_no_tokens");
          else
            error("idref_multiple_tokens");
          break;
        case Datatype.ID_TYPE_IDREFS:
          if (tokens.length > 0) {
            for (int j = 0; j < tokens.length; j++)
              idref(tokens[j]);
          }
          else
            error("idrefs_no_tokens");
          break;
        }
      }
    }
  }

  private void id(String token) throws SAXException {
    Entry entry = (Entry)table.get(token);
    if (entry == null) {
      entry = new Entry();
      table.put(token, entry);
    }
    else if (entry.hadId) {
      error("duplicate_id", token);
      error("first_id", token, entry.idLoc);
      return;
    }
    entry.idLoc = new LocatorImpl(locator);
    entry.hadId = true;
  }

  private void idref(String token) throws SAXException {
    Entry entry = (Entry)table.get(token);
    if (entry == null) {
      entry = new Entry();
      table.put(token, entry);
    }
    if (entry.hadId)
      return;
    if (entry.idrefLocs == null)
      entry.idrefLocs = new Vector();
    entry.idrefLocs.addElement(new LocatorImpl(locator));
  }

  public void endElement(String s, String s1, String s2) throws SAXException {
  }

  public void characters(char[] chars, int i, int i1) throws SAXException {
  }

  public void ignorableWhitespace(char[] chars, int i, int i1) throws SAXException {
  }

  public void processingInstruction(String s, String s1) throws SAXException {
  }

  public void skippedEntity(String s) throws SAXException {
  }

  private void error(String key) throws SAXException {
    hadError = true;
    if (eh != null)
      eh.error(new SAXParseException(Localizer.message(key), locator));
  }

  private void error(String key, String arg) throws SAXException {
    hadError = true;
    if (eh != null)
      eh.error(new SAXParseException(Localizer.message(key, arg), locator));
  }

  private void error(String key, String arg, Locator loc) throws SAXException {
    hadError = true;
    if (eh != null)
      eh.error(new SAXParseException(Localizer.message(key, arg),
                                     loc));
  }

  private static String[] split(String str) {
    int len = str.length();
    int nTokens = 0;
    for (int i = 0; i < len; i++)
     if (!isSpace(str.charAt(i)) && (i == 0 || isSpace(str.charAt(i - 1))))
       nTokens++;
    String[] tokens = new String[nTokens];
    nTokens = 0;
    int tokenStart = -1;
    for (int i = 0; i < len; i++) {
      if (isSpace(str.charAt(i))) {
        if (tokenStart >= 0) {
          tokens[nTokens++] = str.substring(tokenStart, i);
          tokenStart = -1;
        }
      }
      else if (i == 0 || isSpace(str.charAt(i - 1)))
       tokenStart = i;
    }
    if (tokenStart >= 0)
      tokens[nTokens++] = str.substring(tokenStart, len);
    return tokens;
  }

  private static boolean isSpace(char c) {
    switch (c) {
    case ' ':
    case '\r':
    case '\n':
    case '\t':
      return true;
    }
    return false;
  }
}
