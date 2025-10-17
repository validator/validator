package com.thaiopensource.validate;

import java.io.IOException;

import org.xml.sax.SAXException;

import com.thaiopensource.util.PropertyMap;

public interface SchemaResolver {

  public Schema resolveSchema(String systemId, PropertyMap options)
      throws SAXException, IOException, IncorrectSchemaException;

}
