package com.thaiopensource.relaxng.auto;

import com.thaiopensource.relaxng.SchemaLanguage;
import com.thaiopensource.relaxng.Schema;
import com.thaiopensource.relaxng.XMLReaderCreator;
import com.thaiopensource.relaxng.SchemaOptions;
import com.thaiopensource.relaxng.IncorrectSchemaException;
import org.xml.sax.InputSource;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.relaxng.datatype.DatatypeLibraryFactory;

import java.io.IOException;

public class AutoSchemaLanguage implements SchemaLanguage {
  private final SchemaReceiverFactory srf;

  public AutoSchemaLanguage() {
    this(new SchemaReceiverLoader());
  }

  public AutoSchemaLanguage(SchemaReceiverFactory srf) {
    this.srf = srf;
  }

  public Schema createSchema(XMLReaderCreator xrc, InputSource in, ErrorHandler eh, SchemaOptions options, DatatypeLibraryFactory dlf)
          throws IOException, SAXException, IncorrectSchemaException {
    SchemaReceiver sr = new AutoSchemaReceiver(xrc, eh, options, dlf, srf);
    XMLReader xr = xrc.createXMLReader();
    SchemaFuture sf = sr.installHandlers(xr);
    try {
      xr.parse(in);
      return sf.getSchema();
    }
    catch (SAXException e) {
      // Work around broken SAX parsers that catch and wrap runtime exceptions thrown by handlers
      Exception nested = e.getException();
      if (nested instanceof RuntimeException)
        sf.unwrapException((RuntimeException)nested);
      throw e;
    }
    catch (RuntimeException e) {
      throw sf.unwrapException(e);
    }
  }
}
