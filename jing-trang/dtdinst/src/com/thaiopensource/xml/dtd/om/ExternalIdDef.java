package com.thaiopensource.xml.dtd.om;

import com.thaiopensource.xml.em.ExternalId;

public class ExternalIdDef extends Def {
  private final ExternalId externalId;

  public ExternalIdDef(String name, ExternalId externalId) {
    super(name);
    this.externalId = externalId;
  }

  public int getType() {
    return EXTERNAL_ID_DEF;
  }

  public ExternalId getExternalId() {
    return externalId;
  }

  public void accept(TopLevelVisitor visitor) throws Exception {
    visitor.externalIdDef(getName(), externalId);
  }
}
