package com.thaiopensource.relaxng.input.parse.sax;

import com.thaiopensource.relaxng.input.parse.ParseInputFormat;
import com.thaiopensource.relaxng.parse.Parseable;
import com.thaiopensource.relaxng.parse.sax.SAXParseable;
import com.thaiopensource.xml.sax.XMLReaderCreator;
import com.thaiopensource.xml.sax.Jaxp11XMLReaderCreator;
import org.xml.sax.InputSource;
import org.xml.sax.ErrorHandler;

public class SAXParseInputFormat extends ParseInputFormat {
  private final XMLReaderCreator xrc = new Jaxp11XMLReaderCreator();

  public SAXParseInputFormat() {
    super(true);
  }

  public Parseable makeParseable(InputSource in, ErrorHandler eh) {
    return new SAXParseable(xrc, in, eh);
  }
}
