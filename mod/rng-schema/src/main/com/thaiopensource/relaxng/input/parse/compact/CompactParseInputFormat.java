package com.thaiopensource.relaxng.input.parse.compact;

import com.thaiopensource.relaxng.input.parse.ParseInputFormat;
import com.thaiopensource.relaxng.parse.Parseable;
import com.thaiopensource.relaxng.parse.compact.CompactParseable;
import com.thaiopensource.relaxng.parse.compact.UriOpenerImpl;
import com.thaiopensource.xml.sax.Resolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;

public class CompactParseInputFormat extends ParseInputFormat {
  public CompactParseInputFormat() {
    super(false);
  }

  public Parseable makeParseable(InputSource in, Resolver resolver, ErrorHandler eh) {
    return new CompactParseable(in, new UriOpenerImpl(resolver), eh);
  }
}
