package com.thaiopensource.validate.jarv;

import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.Validator;
import org.iso_relax.verifier.Verifier;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class VerifierValidator implements Validator, ErrorHandler {
  private final Verifier verifier;
  private ContentHandler handler;
  private boolean validSoFar = true;
  private final ErrorHandler eh;

  private static class ExceptionReportHandler extends DefaultHandler {
    private final SAXException storedException;

    ExceptionReportHandler(SAXException storedException) {
      this.storedException = storedException;
    }

    public void startDocument()
            throws SAXException {
      throw storedException;
    }
  }

  public VerifierValidator(Verifier verifier, PropertyMap properties) {
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
      handler = new ExceptionReportHandler(e);
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
      handler = new ExceptionReportHandler(e);
    }
    validSoFar = true;
  }

  public ContentHandler getContentHandler() {
    return handler;
  }

  public DTDHandler getDTDHandler() {
    return null;
  }

  public void warning(SAXParseException exception)
          throws SAXException {
    if (eh != null)
      eh.warning(exception);
  }

  public void error(SAXParseException exception)
          throws SAXException {
    validSoFar = false;
    if (eh != null)
      eh.error(exception);
  }

  public void fatalError(SAXParseException exception)
          throws SAXException {
    validSoFar = false;
    if (eh != null)
      eh.fatalError(exception);
  }
}
