package com.thaiopensource.relaxng.input.parse.compact;

import com.thaiopensource.relaxng.input.parse.ParseInputFormat;
import com.thaiopensource.relaxng.parse.Parseable;
import com.thaiopensource.relaxng.parse.compact.CompactParseable;
import org.xml.sax.InputSource;
import org.xml.sax.ErrorHandler;

public class CompactParseInputFormat extends ParseInputFormat {
  public CompactParseInputFormat() {
    super(false);
  }

  public Parseable makeParseable(InputSource in, ErrorHandler eh) {
    return new CompactParseable(in, eh);
  }
}
