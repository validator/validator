package com.thaiopensource.xml.dtd.om;

public class Include extends Flag {

  public int getType() {
    return INCLUDE;
  }

  public void accept(FlagVisitor visitor) throws Exception {
    visitor.include();
  }
}
