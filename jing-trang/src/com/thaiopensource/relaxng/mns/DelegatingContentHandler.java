package com.thaiopensource.relaxng.mns;

import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;

public class DelegatingContentHandler implements ContentHandler {
  private final ContentHandler delegate;

  public DelegatingContentHandler(ContentHandler delegate) {
    this.delegate = delegate;
  }

  public void setDocumentLocator(Locator locator) {
    delegate.setDocumentLocator(locator);
  }

  public void startDocument()
          throws SAXException {
    delegate.startDocument();
  }

  public void endDocument()
          throws SAXException {
    delegate.endDocument();
  }

  public void startPrefixMapping(String prefix, String uri)
          throws SAXException {
    delegate.startPrefixMapping(prefix, uri);
  }

  public void endPrefixMapping(String prefix)
          throws SAXException {
    delegate.endPrefixMapping(prefix);
  }

  public void startElement(String namespaceURI, String localName,
                           String qName, Attributes atts)
          throws SAXException {
    delegate.startElement(namespaceURI, localName, qName, atts);
  }

  public void endElement(String namespaceURI, String localName,
                         String qName)
          throws SAXException {
    delegate.endElement(namespaceURI, localName, qName);
  }

  public void characters(char ch[], int start, int length)
          throws SAXException {
    delegate.characters(ch, start, length);
  }

  public void ignorableWhitespace(char ch[], int start, int length)
          throws SAXException {
    delegate.ignorableWhitespace(ch, start, length);
  }

  public void processingInstruction(String target, String data)
          throws SAXException {
    delegate.processingInstruction(target, data);
  }

  public void skippedEntity(String name)
          throws SAXException {
    delegate.skippedEntity(name);
  }
}
