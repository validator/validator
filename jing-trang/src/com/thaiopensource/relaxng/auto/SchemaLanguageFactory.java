package com.thaiopensource.relaxng.auto;

import com.thaiopensource.relaxng.SchemaLanguage;

public interface SchemaLanguageFactory {
  public SchemaLanguage createSchemaLanguage(String namespaceUri);
}
