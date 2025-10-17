package com.thaiopensource.relaxng.parse.compact;

import com.thaiopensource.relaxng.parse.BuildException;
import com.thaiopensource.xml.sax.Resolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;

public class UriOpenerImpl implements UriOpener {
  private final Resolver resolver;

  public UriOpenerImpl(Resolver resolver) {
    this.resolver = resolver;
  }

  public InputSource resolve(String href, String base) throws BuildException {
    try {
       return resolver.resolve(href, base).getInputSource();
    }
    catch (IOException e) {
      throw new BuildException(e);
    }
    catch (SAXException e) {
      throw BuildException.fromSAXException(e);
    }
  }

  public InputSource open(InputSource in) throws BuildException {
     try {
       return resolver.open(in);
    }
    catch (IOException e) {
      throw new BuildException(e);
    }
    catch (SAXException e) {
      throw BuildException.fromSAXException(e);
    }
  }
}