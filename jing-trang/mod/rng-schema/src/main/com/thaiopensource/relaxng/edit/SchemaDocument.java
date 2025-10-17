package com.thaiopensource.relaxng.edit;

public class SchemaDocument {
  private Pattern pattern;
  private String encoding;

  public SchemaDocument(Pattern pattern) {
    this.pattern = pattern;
  }

  public SchemaDocument(Pattern pattern, String encoding) {
    this.pattern = pattern;
    this.encoding = encoding;
  }

  public Pattern getPattern() {
    return pattern;
  }

  public void setPattern(Pattern pattern) {
    this.pattern = pattern;
  }

  public String getEncoding() {
    return encoding;
  }

  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }
}
