package com.thaiopensource.relaxng.edit;

import java.util.Vector;
import java.util.List;

public class DataPattern extends Pattern {
  private String datatypeLibrary;
  private String type;
  private final List<Param> params = new Vector<Param>();
  private Pattern except;

  public DataPattern(String datatypeLibrary, String type) {
    this.datatypeLibrary = datatypeLibrary;
    this.type = type;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getDatatypeLibrary() {
    return datatypeLibrary;
  }

  public void setDatatypeLibrary(String datatypeLibrary) {
    this.datatypeLibrary = datatypeLibrary;
  }

  public List<Param> getParams() {
    return params;
  }

  public Pattern getExcept() {
    return except;
  }

  public void setExcept(Pattern except) {
    this.except = except;
  }

  public <T> T accept(PatternVisitor<T> visitor) {
    return visitor.visitData(this);
  }
}
