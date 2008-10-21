package com.thaiopensource.relaxng.edit;

public abstract class OpenNameClass extends NameClass {
  private NameClass except;

  public OpenNameClass() {
  }

  public OpenNameClass(NameClass except) {
    this.except = except;
  }

  public NameClass getExcept() {
    return except;
  }

  public void setExcept(NameClass except) {
    this.except = except;
  }
}
