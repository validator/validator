package com.thaiopensource.relaxng.auto;

import org.xml.sax.SAXException;

import java.io.IOException;

import com.thaiopensource.relaxng.Schema;
import com.thaiopensource.relaxng.IncorrectSchemaException;

public interface SchemaFuture {
  Schema getSchema() throws IncorrectSchemaException, SAXException, IOException;
  RuntimeException unwrapException(RuntimeException e) throws SAXException, IOException, IncorrectSchemaException;
}
