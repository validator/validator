package com.thaiopensource.validate.rng;

import com.thaiopensource.relaxng.impl.SchemaReaderImpl;
import com.thaiopensource.relaxng.parse.Parseable;
import com.thaiopensource.relaxng.parse.sax.SAXParseable;
import com.thaiopensource.relaxng.parse.sax.UriResolverImpl;
import com.thaiopensource.validate.SchemaReader;
import com.thaiopensource.xml.sax.Resolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import javax.xml.transform.sax.SAXSource;

public class SAXSchemaReader extends SchemaReaderImpl {
  private static final SchemaReader theInstance = new SAXSchemaReader();
  
  private SAXSchemaReader() {
  }
  
  public static SchemaReader getInstance() {
    return theInstance;
  }

  protected Parseable createParseable(SAXSource source, Resolver resolver, ErrorHandler eh) throws SAXException {
    if (source.getXMLReader() == null)
      source = new SAXSource(resolver.createXMLReader(), source.getInputSource());
    return new SAXParseable(source, new UriResolverImpl(resolver), eh);
  }
}
