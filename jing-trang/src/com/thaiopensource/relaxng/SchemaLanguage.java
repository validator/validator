package com.thaiopensource.relaxng;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.relaxng.datatype.DatatypeLibraryFactory;

import java.io.IOException;

public interface SchemaLanguage {
  Schema createSchema(XMLReaderCreator xrc, InputSource in, ErrorHandler eh, SchemaOptions options, DatatypeLibraryFactory dlf)
          throws IOException, SAXException, IncorrectSchemaException;
}
