package com.thaiopensource.validate;

import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.validate.IncorrectSchemaException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;

public interface SchemaReader {
  Schema createSchema(InputSource in, PropertyMap properties)
          throws IOException, SAXException, IncorrectSchemaException;
}
