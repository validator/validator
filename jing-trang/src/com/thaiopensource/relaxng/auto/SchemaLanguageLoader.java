package com.thaiopensource.relaxng.auto;

import com.thaiopensource.relaxng.SchemaLanguage;
import com.thaiopensource.util.Service;

import java.util.Enumeration;

public class SchemaLanguageLoader implements SchemaLanguageFactory {
  private final Service service = new Service(SchemaLanguageFactory.class);
  public SchemaLanguage createSchemaLanguage(String namespaceUri) {
    for (Enumeration enum = service.getProviders(); enum.hasMoreElements();) {
      SchemaLanguageFactory slf = (SchemaLanguageFactory)enum.nextElement();
      SchemaLanguage sl = slf.createSchemaLanguage(namespaceUri);
      if (sl != null)
        return sl;
    }
    return null;
  }
}
