package com.thaiopensource.xml.dtd;

public class IgnoreSection extends TopLevel {
  
  private final Flag flag;
  private final String contents;

  public IgnoreSection(Flag flag, String contents) {
    this.flag = flag;
    this.contents = contents;
  }

  public int getType() {
    return IGNORE_SECTION;
  }

  public Flag getFlag() {
    return flag;
  }
  
  public String getContents() {
    return contents;
  }

  public void accept(TopLevelVisitor visitor) throws Exception {
    visitor.ignoreSection(flag, contents);
  }

}
