package com.thaiopensource.xml.dtd.om;

public class TokenizedDatatype extends Datatype {
  private final String typeName;

  public TokenizedDatatype(String typeName) {
    this.typeName = typeName;
  }

  public int getType() {
    return TOKENIZED;
  }

  public String getTypeName() {
    return typeName;
  }

  public void accept(DatatypeVisitor visitor) throws Exception {
    visitor.tokenizedDatatype(typeName);
  }
}

