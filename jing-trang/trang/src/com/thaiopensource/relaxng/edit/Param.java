package com.thaiopensource.relaxng.edit;

public class Param extends Annotated {
  private String name;
  private String value;

  public Param(String name, String value) {
    this.name = name;
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
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
}
