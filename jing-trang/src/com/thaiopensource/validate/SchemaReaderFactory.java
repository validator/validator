package com.thaiopensource.validate;

import com.thaiopensource.validate.SchemaReader;

public interface SchemaReaderFactory {
  public SchemaReader createSchemaReader(String namespaceUri);
}
