package com.thaiopensource.validate.jarv;

import com.thaiopensource.validate.ValidatorHandler;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.util.PropertyMap;
import org.iso_relax.verifier.Verifier;
import org.iso_relax.verifier.VerifierHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;
import org.xml.sax.EntityResolver;

public class VerifierValidatorHandler implements ValidatorHandler, ErrorHandler {
  private final Verifier verifier;
  private VerifierHandler handler;
  private boolean validSoFar = true;
  private final ErrorHandler eh;
  private SAXException storedException = null;

  public VerifierValidatorHandler(Verifier verifier, PropertyMap properties) {
    this.verifier = verifier;
    eh = ValidateProperty.ERROR_HANDLER.get(properties);
    verifier.setErrorHandler(this);
    EntityResolver er = ValidateProperty.ENTITY_RESOLVER.get(properties);
    if (er != null)
      verifier.setEntityResolver(er);
    try {
      handler = verifier.getVerifierHandler();
    }
    catch (SAXException e) {
      storedException = e;
    }
  }

  public boolean isValidSoFar() {
    return validSoFar;
  }

  public void reset() {
    try {
      handler = verifier.getVerifierHandler();
    }
    catch (SAXException e) {
      storedException = e;
    }
    validSoFar = true;
  }

  public void setDocumentLocator(Locator locator) {
    if (handler != null)
      handler.setDocumentLocator(locator);
  }

  public void startDocument()
          throws SAXException {
    if (storedException != null)
      throw storedException;
    handler.startDocument();
  }

  public void endDocument()
          throws SAXException {
    handler.endDocument();
    if (!handler.isValid())
      validSoFar = false;
  }

  public void startPrefixMapping(String prefix, String uri)
          throws SAXException {
    handler.startPrefixMapping(prefix, uri);
  }

  public void endPrefixMapping(String prefix)
          throws SAXException {
    handler.endPrefixMapping(prefix);
  }

  public void startElement(String namespaceURI, String localName,
                           String qName, Attributes atts)
          throws SAXException {
    handler.startElement(namespaceURI, localName, qName, atts);
  }

  public void endElement(String namespaceURI, String localName,
                         String qName)
          throws SAXException {
    handler.endElement(namespaceURI, localName, qName);
  }

  public void characters(char ch[], int start, int length)
          throws SAXException {
    handler.characters(ch, start, length);
  }

  public void ignorableWhitespace(char ch[], int start, int length)
          throws SAXException {
    handler.ignorableWhitespace(ch, start, length);
  }

  public void processingInstruction(String target, String data)
          throws SAXException {
    handler.processingInstruction(target, data);
  }

  public void skippedEntity(String name)
          throws SAXException {
    handler.skippedEntity(name);
  }

  public void notationDecl(String name,
                           String publicId,
                           String systemId)
          throws SAXException {
  }

  public void unparsedEntityDecl(String name,
                                 String publicId,
                                 String systemId,
                                 String notationName)
          throws SAXException {
  }

  public void warning (SAXParseException exception)
      throws SAXException {
    if (eh != null)
      eh.warning(exception);
  }

  public void error (SAXParseException exception)
      throws SAXException {
    validSoFar = false;
    if (eh != null)
      eh.error(exception);
  }

  public void fatalError (SAXParseException exception)
      throws SAXException {
    validSoFar = false;
    if (eh != null)
      eh.fatalError(exception);
  }
}
