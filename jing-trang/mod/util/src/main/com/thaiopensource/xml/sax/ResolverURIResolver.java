package com.thaiopensource.xml.sax;

import java.io.IOException;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.xml.sax.SAXException;

public class ResolverURIResolver implements URIResolver {

  private final Resolver delegate;

  /**
   * The constructor.
   * @param delegate the <code>Resolver</code> to delegate to
   */
  public ResolverURIResolver(Resolver delegate) {
    this.delegate = delegate;
  }

  /**
   * Resolves using <code>resolve()</code> on the delegate and wraps exceptions.
   * 
   * @see javax.xml.transform.URIResolver#resolve(java.lang.String, java.lang.String)
   */
  public Source resolve(String href, String base) throws TransformerException {
    try {
      return delegate.resolve(href, base);
    }
    catch (IOException e) {
      throw new TransformerException(e);
    }
    catch (SAXException e) {
      throw new TransformerException(e);
    }
  }
  
}
