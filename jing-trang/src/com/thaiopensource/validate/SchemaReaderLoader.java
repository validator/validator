package com.thaiopensource.validate;

import com.thaiopensource.validate.SchemaReader;
import com.thaiopensource.util.Service;

import java.util.Enumeration;

public class SchemaReaderLoader implements SchemaReaderFactory {
  private final Service service = new Service(SchemaReaderFactory.class);
  public SchemaReader createSchemaReader(String namespaceUri) {
    for (Enumeration enum = service.getProviders(); enum.hasMoreElements();) {
      SchemaReaderFactory srf = (SchemaReaderFactory)enum.nextElement();
      SchemaReader sr = srf.createSchemaReader(namespaceUri);
      if (sr != null)
        return sr;
    }
    return null;
  }
}
