package com.thaiopensource.relaxng;

import com.thaiopensource.relaxng.impl.SchemaLanguageImpl;
import com.thaiopensource.relaxng.parse.Parseable;
import com.thaiopensource.relaxng.parse.sax.SAXParseable;
import org.xml.sax.InputSource;
import org.xml.sax.ErrorHandler;

public class SAXSchemaLanguage extends SchemaLanguageImpl {
  private static SchemaLanguage theInstance = new SAXSchemaLanguage();
  
  private SAXSchemaLanguage() {
  }
  
  public static SchemaLanguage getInstance() {
    return theInstance;
  }

  protected Parseable createParseable(XMLReaderCreator xrc, InputSource in, ErrorHandler eh) {
    return new SAXParseable(xrc, in, eh);
  }
}
