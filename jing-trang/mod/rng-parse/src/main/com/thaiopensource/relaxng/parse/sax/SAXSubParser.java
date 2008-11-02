package com.thaiopensource.relaxng.parse.sax;

import com.thaiopensource.relaxng.parse.BuildException;
import com.thaiopensource.relaxng.parse.SubParseable;
import com.thaiopensource.relaxng.parse.SubParser;
import org.xml.sax.ErrorHandler;

public class SAXSubParser implements SubParser {
  final UriResolver resolver;
  final ErrorHandler eh;

  SAXSubParser(UriResolver resolver, ErrorHandler eh) {
    this.resolver = resolver;
    this.eh = eh;
  }

  public SubParseable createSubParseable(String href, String base) throws BuildException {
    return new SAXParseable(resolver.resolve(href, base), resolver, eh);
  }
}
