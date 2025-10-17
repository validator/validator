package com.thaiopensource.relaxng.parse.sax;

import com.thaiopensource.relaxng.parse.ParseReceiver;
import com.thaiopensource.relaxng.parse.ParsedPatternFuture;
import com.thaiopensource.relaxng.parse.SchemaBuilder;
import com.thaiopensource.relaxng.parse.Scope;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class SAXParseReceiver extends SAXSubParser implements ParseReceiver {
  public SAXParseReceiver(UriResolver resolver, ErrorHandler eh) {
    super(resolver, eh);
  }

  public ParsedPatternFuture installHandlers(XMLReader xr, SchemaBuilder schemaBuilder, Scope scope)
          throws SAXException {
    return new SchemaParser(xr, eh, schemaBuilder, null, scope);
  }
}
