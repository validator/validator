package com.thaiopensource.xml.dtd;

public class IgnoredSection extends TopLevel {
  private final String value;

  IgnoredSection(String value) {
    this.value = value;
  }

  public int getType() {
    return IGNORED_SECTION;
  }
      
  public String getValue() {
    return value;
  }

  public void accept(TopLevelVisitor visitor) throws Exception {
    visitor.ignoredSection(value);
  }
}
