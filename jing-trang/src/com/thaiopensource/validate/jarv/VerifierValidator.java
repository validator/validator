package com.thaiopensource.validate.jarv;

import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.Validator;
import org.iso_relax.verifier.Verifier;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class VerifierValidator implements Validator {
  private final Verifier verifier;
  private ContentHandler handler;

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
    verifier.setErrorHandler(ValidateProperty.ERROR_HANDLER.get(properties));
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

  public void reset() {
    try {
      handler = verifier.getVerifierHandler();
    }
    catch (SAXException e) {
      handler = new ExceptionReportHandler(e);
    }
  }

  public ContentHandler getContentHandler() {
    return handler;
  }

  public DTDHandler getDTDHandler() {
    return null;
  }
}
