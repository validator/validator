package com.thaiopensource.xml.dtd.om;

import com.thaiopensource.xml.em.ExternalId;

public class ExternalIdRef extends TopLevel {

  private final String name;
  private final ExternalId externalId;
  private final TopLevel[] contents;

  public ExternalIdRef(String name,
		       ExternalId externalId,
		       TopLevel[] contents) {
    this.name = name;
    this.externalId = externalId;
    this.contents = contents;
  }

  public int getType() {
    return EXTERNAL_ID_REF;
  }

  public String getName() {
    return name;
  }

  public ExternalId getExternalId() {
    return externalId;
  }

  public TopLevel[] getContents() {
    TopLevel[] tem = new TopLevel[contents.length];
    System.arraycopy(contents, 0, tem, 0, contents.length);
    return tem;
  }

  public void accept(TopLevelVisitor visitor) throws Exception {
    visitor.externalIdRef(name, externalId, getContents());
  }

}
