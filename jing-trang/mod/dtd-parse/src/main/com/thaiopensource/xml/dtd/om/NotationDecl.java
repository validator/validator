package com.thaiopensource.xml.dtd.om;

import com.thaiopensource.xml.em.ExternalId;

public class NotationDecl extends TopLevel {
  
  private final String name;
  private final ExternalId externalId;

  public NotationDecl(String name, ExternalId externalId) { 
    this.name = name;
    this.externalId = externalId;
  }

  public int getType() {
    return NOTATION_DECL;
  }
  
  public String getName() {
    return name;
  }

  public ExternalId getExternalId() {
    return externalId;
  }

  public void accept(TopLevelVisitor visitor) throws Exception {
    visitor.notationDecl(name, externalId);
  }
}
