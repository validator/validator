package com.thaiopensource.relaxng;

import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;

public class DelegateContentHandler implements ContentHandler {
  private ContentHandler contentHandler;

  DelegateContentHandler() {
    this.contentHandler = null;
  }

  DelegateContentHandler(ContentHandler contentHandler) {
    this.contentHandler = contentHandler;
  }

  public void setDocumentLocator(Locator locator) {
    if (contentHandler != null)
      contentHandler.setDocumentLocator(locator);
  }
  
  public void startDocument() throws SAXException {
    if (contentHandler != null)
      contentHandler.startDocument();
  }
  
  public void endDocument() throws SAXException {
    if (contentHandler != null)
      contentHandler.endDocument();
  }

  public void startPrefixMapping(String prefix, String uri) throws SAXException {
    if (contentHandler != null)
      contentHandler.startPrefixMapping(prefix, uri);
  }

  public void endPrefixMapping(String prefix) throws SAXException {
    if (contentHandler != null)
      contentHandler.endPrefixMapping(prefix);
  }

  public void startElement(String namespaceURI, String localName,
			   String qName, Attributes atts) throws SAXException {
    if (contentHandler != null)
      contentHandler.startElement(namespaceURI, localName,
			    qName, atts);
  }

  public void endElement(String namespaceURI, String localName,
			 String qName) throws SAXException {
    if (contentHandler != null)
      contentHandler.endElement(namespaceURI, localName, qName);
  }

  public void characters(char ch[],
			 int start, int length) throws SAXException {
    if (contentHandler != null)
      contentHandler.characters(ch, start, length);
  }

  public void ignorableWhitespace (char ch[],
				   int start, int length) throws SAXException {
    if (contentHandler != null)
      contentHandler.ignorableWhitespace(ch, start, length);
  }


  public void processingInstruction(String target, String data)
    throws SAXException {
    if (contentHandler != null)
      contentHandler.processingInstruction(target, data);
  }

  public void skippedEntity(String name) throws SAXException {
    if (contentHandler != null)
      contentHandler.skippedEntity(name);
  }

  public ContentHandler getDelegate() {
    return contentHandler;
  }

  public void setDelegate(ContentHandler contentHandler) {
    this.contentHandler = contentHandler;
  }
}
