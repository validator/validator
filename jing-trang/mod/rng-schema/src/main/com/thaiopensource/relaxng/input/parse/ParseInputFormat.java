package com.thaiopensource.relaxng.input.parse;

import com.thaiopensource.datatype.DatatypeLibraryLoader;
import com.thaiopensource.relaxng.edit.SchemaCollection;
import com.thaiopensource.relaxng.input.InputFailedException;
import com.thaiopensource.relaxng.input.InputFormat;
import com.thaiopensource.relaxng.parse.IllegalSchemaException;
import com.thaiopensource.relaxng.parse.Parseable;
import com.thaiopensource.relaxng.translate.util.EncodingParam;
import com.thaiopensource.relaxng.translate.util.InvalidParamsException;
import com.thaiopensource.relaxng.translate.util.ParamProcessor;
import com.thaiopensource.relaxng.translate.util.ResolverParam;
import com.thaiopensource.xml.sax.Resolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;

public abstract class ParseInputFormat implements InputFormat {
  private final boolean commentsNeedTrimming;
  protected ParseInputFormat(boolean commentsNeedTrimming) {
    this.commentsNeedTrimming = commentsNeedTrimming;
  }

  static private class Options {
    Resolver resolver;
  }
  public SchemaCollection load(String uri, String[] params, String outputFormat, ErrorHandler eh, ClassLoader loader)
          throws InputFailedException, InvalidParamsException, IOException, SAXException {
    final InputSource in = new InputSource(uri);
    final Options options = new Options();
    ParamProcessor pp = new ParamProcessor();
    pp.declare("encoding",
               new EncodingParam() {
                 protected void setEncoding(String encoding) {
                   in.setEncoding(encoding);
                 }
               });
    pp.declare("resolver",
               new ResolverParam(loader) {
                 protected void setResolver(Resolver resolver) {
                   options.resolver = resolver;
                 }
               });
    pp.process(params, eh);
    Resolver resolver = options.resolver;
    if (resolver == null)
      resolver = Resolver.newInstance();
    Parseable parseable = makeParseable(in, resolver, eh);
    try {
      return SchemaBuilderImpl.parse(parseable,
                                     uri,
                                     eh,
                                     new DatatypeLibraryLoader(),
                                     commentsNeedTrimming);
    }
    catch (IllegalSchemaException e) {
      throw new InputFailedException();
    }
  }

  protected abstract Parseable makeParseable(InputSource in, Resolver resolver, ErrorHandler eh) throws SAXException;
}
