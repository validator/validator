package com.thaiopensource.relaxng.edit;

import java.util.Vector;
import java.util.List;

public class DataPattern extends Pattern {
  private String datatypeLibrary;
  private String type;
  private final List params = new Vector();
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

  public List getParams() {
    return params;
  }

  public Pattern getExcept() {
    return except;
  }

  public void setExcept(Pattern except) {
    this.except = except;
  }

  public Object accept(PatternVisitor visitor) {
    return visitor.visitData(this);
  }
}
