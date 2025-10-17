package com.thaiopensource.xml.dtd.om;

public class IgnoredSection extends TopLevel {
  
  private final Flag flag;
  private final String contents;

  public IgnoredSection(Flag flag, String contents) {
    this.flag = flag;
    this.contents = contents;
  }

  public int getType() {
    return IGNORED_SECTION;
  }

  public Flag getFlag() {
    return flag;
  }
  
  public String getContents() {
    return contents;
  }

  public void accept(TopLevelVisitor visitor) throws Exception {
    visitor.ignoredSection(flag, contents);
  }

}
