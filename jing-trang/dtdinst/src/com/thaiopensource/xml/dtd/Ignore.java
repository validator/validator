package com.thaiopensource.xml.dtd;

public class Ignore extends Flag {

  public int getType() {
    return IGNORE;
  }

  public void accept(FlagVisitor visitor) throws Exception {
    visitor.ignore();
  }
}
