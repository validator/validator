package com.thaiopensource.relaxng;

import com.thaiopensource.relaxng.impl.SchemaLanguageImpl;
import com.thaiopensource.relaxng.parse.Parseable;
import com.thaiopensource.relaxng.parse.compact.CompactParseable;
import org.xml.sax.InputSource;
import org.xml.sax.ErrorHandler;

public class CompactSchemaLanguage extends SchemaLanguageImpl {
  private static SchemaLanguage theInstance = new CompactSchemaLanguage();

  private CompactSchemaLanguage() {
  }

  public static SchemaLanguage getInstance() {
    return theInstance;
  }

  protected Parseable createParseable(XMLReaderCreator xrc, InputSource in, ErrorHandler eh) {
    return new CompactParseable(in, eh);
  }
}
