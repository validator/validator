package com.thaiopensource.relaxng.xerces;

import com.thaiopensource.relaxng.auto.SchemaLanguageFactory;
import com.thaiopensource.relaxng.SchemaLanguage;
import com.thaiopensource.xml.util.WellKnownNamespaces;

public class XsdSchemaLanguageFactory implements SchemaLanguageFactory {
  public SchemaLanguage createSchemaLanguage(String namespaceUri) {
    if (WellKnownNamespaces.XML_SCHEMA.equals(namespaceUri))
      return new SchemaLanguageImpl();
    return null;
  }
}
