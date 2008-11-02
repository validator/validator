package com.thaiopensource.validate;

import com.thaiopensource.util.PropertyMap;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.transform.sax.SAXSource;
import java.io.IOException;


public abstract class AbstractSchemaReader implements SchemaReader {
  public Schema createSchema(InputSource in, PropertyMap properties) throws IOException, SAXException, IncorrectSchemaException {
    return createSchema(new SAXSource(in), properties);
  }
}
