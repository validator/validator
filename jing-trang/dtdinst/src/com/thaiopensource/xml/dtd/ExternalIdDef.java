package com.thaiopensource.xml.dtd;

public class ExternalIdDef extends TopLevel {
  private final String name;
  private final ExternalId externalId;

  public ExternalIdDef(String name, ExternalId externalId) {
    this.name = name;
    this.externalId = externalId;
  }

  public int getType() {
    return EXTERNAL_ID_DEF;
  }

  public String getName() {
    return name;
  }

  public ExternalId getExternalId() {
    return externalId;
  }

  public void accept(TopLevelVisitor visitor) throws Exception {
    visitor.externalIdDef(name, externalId);
  }
}
