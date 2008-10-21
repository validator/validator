package com.thaiopensource.relaxng.edit;

import java.util.Map;
import java.util.HashMap;

public class ValuePattern extends Pattern {
  private String datatypeLibrary;
  private String type;
  private String value;
  private final Map<String, String> prefixMap = new HashMap<String, String>();

  public ValuePattern(String datatypeLibrary, String type, String value) {
    this.datatypeLibrary = datatypeLibrary;
    this.type = type;
    this.value = value;
  }

  public String getDatatypeLibrary() {
    return datatypeLibrary;
  }

  public void setDatatypeLibrary(String datatypeLibrary) {
    this.datatypeLibrary = datatypeLibrary;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public boolean mayContainText() {
    return true;
  }

  public Map<String, String> getPrefixMap() {
    return prefixMap;
  }

  public <T> T accept(PatternVisitor<T> visitor) {
    return visitor.visitValue(this);
  }
}
