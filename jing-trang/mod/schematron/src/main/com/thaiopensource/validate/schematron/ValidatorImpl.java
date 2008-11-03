package com.thaiopensource.validate.schematron;

import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.validate.ResolverFactory;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.Validator;
import com.thaiopensource.xml.sax.Resolver;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.ErrorHandler;

import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;

class ValidatorImpl implements Validator {
  private final Templates templates;
  private final SAXTransformerFactory factory;
  private final ContentHandler outputHandler;
  private TransformerHandler transformerHandler;
  private final Resolver resolver;

  ValidatorImpl(Templates templates, SAXTransformerFactory factory, PropertyMap properties) {
    this.templates = templates;
    this.factory = factory;
    ErrorHandler eh = ValidateProperty.ERROR_HANDLER.get(properties);
    outputHandler = new OutputHandler(eh);
    resolver = ResolverFactory.createResolver(properties);
    initTransformerHandler();
  }

  public ContentHandler getContentHandler() {
    return transformerHandler;
  }

  public DTDHandler getDTDHandler() {
    return transformerHandler;
  }

  public void reset() {
    initTransformerHandler();
  }

  private void initTransformerHandler() {
    try {
      transformerHandler = factory.newTransformerHandler(templates);
      URIResolver uriResolver = resolver.getUriResolver();
      if (uriResolver != null)
        transformerHandler.getTransformer().setURIResolver(uriResolver);
      // XXX set up transformer with an ErrorListener that just throws
      // XXX (what about errors from document() calls?)
    }
    catch (TransformerConfigurationException e) {
      throw new RuntimeException("could not create transformer");
    }
    transformerHandler.setResult(new SAXResult(outputHandler));
  }
}
