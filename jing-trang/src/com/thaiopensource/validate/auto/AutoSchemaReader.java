package com.thaiopensource.validate.auto;

import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.IncorrectSchemaException;
import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.SchemaReader;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.Option;
import com.thaiopensource.xml.sax.XMLReaderCreator;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;

public class AutoSchemaReader implements SchemaReader {
  private final SchemaReceiverFactory srf;

  public AutoSchemaReader() {
    this(new SchemaReceiverLoader());
  }

  public AutoSchemaReader(SchemaReceiverFactory srf) {
    this.srf = srf == null ? new SchemaReceiverLoader() : srf;
  }

  public Schema createSchema(InputSource in, PropertyMap properties)
          throws IOException, SAXException, IncorrectSchemaException {
    if (SchemaReceiverFactory.PROPERTY.get(properties) != srf) {
      PropertyMapBuilder builder = new PropertyMapBuilder(properties);
      SchemaReceiverFactory.PROPERTY.put(builder, srf);
      properties = builder.toPropertyMap();
    }
    SchemaReceiver sr = new AutoSchemaReceiver(properties);
    XMLReaderCreator xrc = ValidateProperty.XML_READER_CREATOR.get(properties);
    XMLReader xr = xrc.createXMLReader();
    ErrorHandler eh = ValidateProperty.ERROR_HANDLER.get(properties);
    if (eh != null)
      xr.setErrorHandler(eh);
    SchemaFuture sf = sr.installHandlers(xr);
    // XXX we should wrap the input source so that we don't have to read it twice
    try {
      ReparseException reparser;
      try {
        xr.parse(in);
        return sf.getSchema();
      }
      catch (ReparseException e) {
        reparser = e;
      }
      for (;;) {
        try {
          return reparser.reparse(in);
        }
        catch (ReparseException e) {
          reparser = e;
        }
      }
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

  public Option getOption(String uri) {
    return srf.getOption(uri);
  }
}
