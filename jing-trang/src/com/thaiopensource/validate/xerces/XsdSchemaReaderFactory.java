package com.thaiopensource.validate.xerces;

import com.thaiopensource.validate.SchemaReaderFactory;
import com.thaiopensource.validate.SchemaReader;
import com.thaiopensource.validate.xerces.SchemaReaderImpl;
import com.thaiopensource.xml.util.WellKnownNamespaces;

public class XsdSchemaReaderFactory implements SchemaReaderFactory {
  public SchemaReader createSchemaReader(String namespaceUri) {
    if (WellKnownNamespaces.XML_SCHEMA.equals(namespaceUri))
      return new SchemaReaderImpl();
    return null;
  }
}
