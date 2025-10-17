package com.thaiopensource.relaxng.input.xml;

import com.thaiopensource.relaxng.edit.SchemaCollection;
import com.thaiopensource.relaxng.input.AbstractMultiInputFormat;
import com.thaiopensource.relaxng.input.InputFailedException;
import com.thaiopensource.relaxng.translate.util.EncodingParam;
import com.thaiopensource.relaxng.translate.util.ResolverParam;
import com.thaiopensource.relaxng.translate.util.InvalidParamsException;
import com.thaiopensource.relaxng.translate.util.ParamProcessor;
import com.thaiopensource.xml.sax.Resolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import java.io.IOException;

public class XmlInputFormat extends AbstractMultiInputFormat {
  public SchemaCollection load(String[] uris, String[] params, String outputFormat, ErrorHandler eh, ClassLoader loader)
          throws InputFailedException, InvalidParamsException, IOException, SAXException {
    ParamProcessor pp = new ParamProcessor();
    final Inferrer.Options options = new Inferrer.Options();
    pp.declare("encoding",
               new EncodingParam() {
                 protected void setEncoding(String encoding) {
                   options.encoding = encoding;
                 }
               });
    pp.declare("resolver",
               new ResolverParam(loader) {
                 protected void setResolver(Resolver resolver) {
                   options.resolver = resolver;
                 }
               });
    pp.process(params, eh);
    return Inferrer.infer(uris, options, eh);
  }
}
