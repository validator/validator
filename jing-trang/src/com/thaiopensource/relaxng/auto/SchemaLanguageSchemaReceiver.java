package com.thaiopensource.relaxng.auto;

import com.thaiopensource.relaxng.SchemaLanguage;
import com.thaiopensource.relaxng.XMLReaderCreator;
import com.thaiopensource.relaxng.SchemaOptions;
import com.thaiopensource.relaxng.Schema;
import com.thaiopensource.relaxng.IncorrectSchemaException;
import org.xml.sax.ErrorHandler;
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.relaxng.datatype.DatatypeLibraryFactory;

import java.io.IOException;

public class SchemaLanguageSchemaReceiver implements SchemaReceiver {
 private final SchemaLanguage schemaLanguage;
  private final XMLReaderCreator xrc;
  private final ErrorHandler eh;
  private final SchemaOptions options;
  private final DatatypeLibraryFactory dlf;

  public SchemaLanguageSchemaReceiver(SchemaLanguage schemaLanguage, XMLReaderCreator xrc, ErrorHandler eh, SchemaOptions options, DatatypeLibraryFactory dlf) {
    this.schemaLanguage = schemaLanguage;
    this.xrc = xrc;
    this.eh = eh;
    this.options = options;
    this.dlf = dlf;
  }

  public SchemaFuture installHandlers(XMLReader xr) throws SAXException {
    throw new ReparseException() {
      public Schema reparse(InputSource in) throws IncorrectSchemaException, SAXException, IOException {
        return schemaLanguage.createSchema(xrc, in, eh, options, dlf);
      }
    };
  }
}
