package com.thaiopensource.relaxng.edit;

import java.util.Map;
import java.util.HashMap;

public class SchemaCollection {
  private final Map schemas = new HashMap();
  private Pattern mainSchema;

  public SchemaCollection() {
  }

  public Pattern getMainSchema() {
    return mainSchema;
  }

  public void setMainSchema(Pattern mainSchema) {
    this.mainSchema = mainSchema;
  }

  public Map getSchemas() {
    return schemas;
  }
}
