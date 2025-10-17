package com.thaiopensource.relaxng.parse.sax;

import com.thaiopensource.relaxng.parse.BuildException;
import com.thaiopensource.xml.sax.Resolver;
import org.xml.sax.SAXException;

import javax.xml.transform.sax.SAXSource;
import java.io.IOException;

public class UriResolverImpl implements UriResolver {
  private final Resolver resolver;

  public UriResolverImpl(Resolver resolver) {
    this.resolver = resolver;
  }

  public SAXSource resolve(String href, String base) throws BuildException {
    try {
      SAXSource source = resolver.resolve(href, base);
      if (source.getXMLReader() == null)
        source = new SAXSource(resolver.createXMLReader(), source.getInputSource());
      return source;
    }
    catch (IOException e) {
      throw new BuildException(e);
    }
    catch (SAXException e) {
      throw BuildException.fromSAXException(e);
    }
  }
}
