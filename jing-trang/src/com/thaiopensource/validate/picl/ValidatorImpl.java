package com.thaiopensource.validate.picl;

import com.thaiopensource.validate.Validator;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.util.Localizer;
import com.thaiopensource.util.PropertyMap;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.LocatorImpl;
import org.xml.sax.ContentHandler;
import org.xml.sax.Attributes;
import org.xml.sax.DTDHandler;
import org.xml.sax.SAXException;
import org.xml.sax.Locator;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

import java.util.Stack;

class ValidatorImpl extends DefaultHandler implements Validator, Path, PatternManager, ErrorContext {
  private final Constraint constraint;
  private final Stack openElements = new Stack();
  private final Stack valueHandlers = new Stack();
  private final Stack activePatterns = new Stack();
  private final AttributePath attributePath = new AttributePath();
  private Locator locator;
  private final ErrorHandler eh;
  private final Localizer localizer = new Localizer(ValidatorImpl.class);

  private static class WrappedSAXException extends RuntimeException {
    final SAXException exception;

    WrappedSAXException(SAXException exception) {
      this.exception = exception;
    }
  }

  static class ActivePattern {
    final int rootDepth;
    final Pattern pattern;
    final SelectionHandler handler;

    ActivePattern(int rootDepth, Pattern pattern, SelectionHandler handler) {
      this.rootDepth = rootDepth;
      this.pattern = pattern;
      this.handler = handler;
    }
  }

  static class OpenElement {
    final String namespaceUri;
    final String localName;
    int nActivePatterns;
    int nValueHandlers;

    OpenElement(String namespaceUri, String localName) {
      this.namespaceUri = namespaceUri;
      this.localName = localName;
    }
  }

  class AttributePath implements Path {
    private Attributes atts;
    private int attIndex;

    void set(Attributes atts, int attIndex) {
      this.atts = atts;
      this.attIndex = attIndex;
    }

    public boolean isAttribute() {
      return true;
    }

    public int length() {
      return ValidatorImpl.this.length() + 1;
    }

    public String getLocalName(int i) {
      if (i == openElements.size())
        return atts.getLocalName(attIndex);
      return ValidatorImpl.this.getLocalName(i);
    }

    public String getNamespaceUri(int i) {
      if (i == openElements.size())
        return atts.getURI(attIndex);
      return ValidatorImpl.this.getNamespaceUri(i);
    }
  }

  ValidatorImpl(Constraint constraint, PropertyMap properties) {
    this.constraint = constraint;
    this.eh = ValidateProperty.ERROR_HANDLER.get(properties);
  }

  public ContentHandler getContentHandler() {
    return this;
  }

  public DTDHandler getDTDHandler() {
    return null;
  }

  public void reset() {
    openElements.setSize(0);
    valueHandlers.setSize(0);
    activePatterns.setSize(0);
    locator = null;
  }

  public int length() {
    return openElements.size();
  }

  public String getLocalName(int i) {
    return ((OpenElement)openElements.elementAt(i)).localName;
  }

  public String getNamespaceUri(int i) {
    return ((OpenElement)openElements.elementAt(i)).namespaceUri;
  }

  public boolean isAttribute() {
    return false;
  }

  public void registerPattern(Pattern pattern, SelectionHandler handler) {
    // XXX what about case where it matches dot?
    activePatterns.push(new ActivePattern(openElements.size(), pattern, handler));
    ((OpenElement)openElements.peek()).nActivePatterns += 1;
  }

  public void registerValueHandler(ValueHandler handler) {
    valueHandlers.push(handler);
    ((OpenElement)openElements.peek()).nValueHandlers += 1;
  }

  public void setDocumentLocator(Locator locator) {
    this.locator = locator;
  }

  public void startDocument() throws SAXException {
    if (locator == null) {
      LocatorImpl tem = new LocatorImpl();
      tem.setLineNumber(-1);
      tem.setColumnNumber(-1);
      locator = tem;
    }
    openElements.push(new OpenElement("", "#root"));
    try {
      constraint.activate(this);
    }
    catch (WrappedSAXException e) {
      throw e.exception;
    }
  }

  public void endDocument() throws SAXException {
    try {
      popOpenElement();
    }
    catch (WrappedSAXException e) {
      throw e.exception;
    }
  }

  public void startElement(String uri, String localName,
                           String qName, Attributes attributes)
          throws SAXException {
    try {
      openElements.push(new OpenElement(uri, localName));
      for (int i = 0, len = valueHandlers.size(); i < len; i++)
        ((ValueHandler)valueHandlers.elementAt(i)).tag(this);
      for (int i = 0, len = activePatterns.size(); i < len; i++) {
        ActivePattern ap = (ActivePattern)activePatterns.elementAt(i);
        if (ap.pattern.matches(this, ap.rootDepth))
          ap.handler.selectElement(this, this, this);
      }
      int nActivePatterns = activePatterns.size();
      for (int i = 0, len = attributes.getLength(); i < len; i++) {
        attributePath.set(attributes, i);
        for (int j = 0; j < nActivePatterns; j++) {
          ActivePattern ap = (ActivePattern)activePatterns.elementAt(j);
          if (ap.pattern.matches(attributePath, ap.rootDepth))
            ap.handler.selectAttribute(this, attributePath, attributes.getValue(i));
        }
      }
    }
    catch (WrappedSAXException e) {
      throw e.exception;
    }
  }

  public void endElement(String uri, String localName, String qName)
          throws SAXException {
    try {
      popOpenElement();
    }
    catch (WrappedSAXException e) {
      throw e.exception;
    }
  }

  public void characters(char ch[], int start, int length)
          throws SAXException {
    try {
      for (int i = 0, len = valueHandlers.size(); i < len; i++)
        ((ValueHandler)valueHandlers.elementAt(i)).characters(this, ch, start, length);
    }
    catch (WrappedSAXException e) {
      throw e.exception;
    }
  }

  public void ignorableWhitespace(char ch[], int start, int length)
          throws SAXException {
    characters(ch, start, length);
  }

  private void popOpenElement() {
    OpenElement top = (OpenElement)openElements.pop();
    for (int i = 0; i < top.nValueHandlers; i++) {
      ValueHandler h = (ValueHandler)valueHandlers.pop();
      h.valueComplete(this);
    }
    for (int i = 0; i < top.nActivePatterns; i++) {
      ActivePattern ap = (ActivePattern)activePatterns.pop();
      ap.handler.selectComplete(this);
    }
  }

  public void error(String key) {
    error(key, locator);
  }

  public void error(String key, String arg) {
    error(key, arg, locator);
  }

  public void error(String key, Locator locator) {
    try {
      eh.error(new SAXParseException(localizer.message(key), locator));
    }
    catch (SAXException e) {
      throw new WrappedSAXException(e);
    }
  }

  public void error(String key, String arg, Locator locator) {
    try {
      eh.error(new SAXParseException(localizer.message(key, arg), locator));
    }
    catch (SAXException e) {
      throw new WrappedSAXException(e);
    }
  }

  public Locator saveLocator() {
    return new LocatorImpl(locator);
  }
}
