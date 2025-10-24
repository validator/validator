package com.thaiopensource.xml.dtd.om;

import com.thaiopensource.xml.em.ExternalId;

public class ExternalEntityDecl extends TopLevel {
  
  private final String name;
  private final ExternalId externalId;

  public ExternalEntityDecl(String name, ExternalId externalId) { 
    this.name = name;
    this.externalId = externalId;
  }

  public int getType() {
    return EXTERNAL_ENTITY_DECL;
  }
  
  public String getName() {
    return name;
  }

  public ExternalId getExternalId() {
    return externalId;
  }

  public void accept(TopLevelVisitor visitor) throws Exception {
    visitor.externalEntityDecl(name, externalId);
  }
}
