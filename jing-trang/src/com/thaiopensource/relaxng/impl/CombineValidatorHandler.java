package com.thaiopensource.relaxng.impl;

import com.thaiopensource.validate.ValidatorHandler;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class CombineValidatorHandler implements ValidatorHandler {
  private final ValidatorHandler vh1;
  private final ValidatorHandler vh2;

  CombineValidatorHandler(ValidatorHandler ch1, ValidatorHandler ch2) {
    this.vh1 = ch1;
    this.vh2 = ch2;
  }

  public void setDocumentLocator(Locator locator) {
    vh1.setDocumentLocator(locator);
    vh2.setDocumentLocator(locator);
  }

  public void startDocument() throws SAXException {
    vh1.startDocument();
    vh2.startDocument();
  }

  public void endDocument() throws SAXException {
    vh1.endDocument();
    vh2.endDocument();
  }

  public void startPrefixMapping(String s, String s1) throws SAXException {
    vh1.startPrefixMapping(s, s1);
    vh2.startPrefixMapping(s, s1);
  }

  public void endPrefixMapping(String s) throws SAXException {
    vh1.endPrefixMapping(s);
    vh2.endPrefixMapping(s);
  }

  public void startElement(String s, String s1, String s2, Attributes attributes) throws SAXException {
    vh1.startElement(s, s1, s2, attributes);
    vh2.startElement(s, s1, s2, attributes);
  }

  public void endElement(String s, String s1, String s2) throws SAXException {
    vh1.endElement(s, s1, s2);
    vh2.endElement(s, s1, s2);
  }

  public void characters(char[] chars, int i, int i1) throws SAXException {
    vh1.characters(chars, i, i1);
    vh2.characters(chars, i, i1);
  }

  public void ignorableWhitespace(char[] chars, int i, int i1) throws SAXException {
    vh1.ignorableWhitespace(chars, i, i1);
    vh2.ignorableWhitespace(chars, i, i1);
  }

  public void processingInstruction(String s, String s1) throws SAXException {
    vh1.processingInstruction(s, s1);
    vh2.processingInstruction(s, s1);
  }

  public void skippedEntity(String s) throws SAXException {
    vh1.skippedEntity(s);
    vh2.skippedEntity(s);
  }

  public void notationDecl(String name,
                           String publicId,
                           String systemId)
          throws SAXException {
    vh1.notationDecl(name, publicId, systemId);
    vh2.notationDecl(name, publicId, systemId);
  }

  public void unparsedEntityDecl(String name,
                                 String publicId,
                                 String systemId,
                                 String notationName)
          throws SAXException {
    vh1.unparsedEntityDecl(name, publicId, systemId, notationName);
    vh2.unparsedEntityDecl(name, publicId, systemId, notationName);
  }

  public void reset() {
    vh1.reset();
    vh2.reset();
  }

  public boolean isValidSoFar() {
    return vh1.isValidSoFar() && vh2.isValidSoFar();
  }

}
