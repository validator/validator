package com.thaiopensource.xml.dtd;

public class Include extends Flag {

  public int getType() {
    return INCLUDE;
  }

  public void accept(FlagVisitor visitor) throws Exception {
    visitor.include();
  }
}
