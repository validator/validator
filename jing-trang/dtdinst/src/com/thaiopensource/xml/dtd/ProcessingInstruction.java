package com.thaiopensource.xml.dtd;

public class ProcessingInstruction extends TopLevel {
  private final String target;
  private final String value;

  ProcessingInstruction(String target, String value) {
    this.target = target;
    this.value = value;
  }

  public int getType() {
    return PROCESSING_INSTRUCTION;
  }
      
  public String getTarget() {
    return target;
  }

  public String getValue() {
    return value;
  }

  public void accept(TopLevelVisitor visitor) throws Exception {
    visitor.processingInstruction(target, value);
  }
}
