package com.thaiopensource.relaxng.edit;

import java.util.Map;
import java.util.HashMap;

public class SchemaCollection {
  private final Map schemaDocumentMap = new HashMap();
  private String mainUri;

  public SchemaCollection() {
  }

  public String getMainUri() {
    return mainUri;
  }

  public void setMainUri(String mainUri) {
    this.mainUri = mainUri;
  }

  public Map getSchemaDocumentMap() {
    return schemaDocumentMap;
  }
}
