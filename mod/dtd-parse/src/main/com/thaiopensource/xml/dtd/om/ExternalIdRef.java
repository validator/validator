package com.thaiopensource.xml.dtd.om;

import com.thaiopensource.xml.em.ExternalId;

public class ExternalIdRef extends TopLevel {

  private final String name;
  private final ExternalId externalId;
  private final String uri;
  private final String encoding;
  private final TopLevel[] contents;

  public ExternalIdRef(String name,
		       ExternalId externalId,
		       String uri,
		       String encoding,
		       TopLevel[] contents) {
    this.name = name;
    this.externalId = externalId;
    this.uri = uri;
    this.encoding = encoding;
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

  public String getUri() {
    return uri;
  }

  public String getEncoding() {
    return encoding;
  }

  public TopLevel[] getContents() {
    TopLevel[] tem = new TopLevel[contents.length];
    System.arraycopy(contents, 0, tem, 0, contents.length);
    return tem;
  }

  public void accept(TopLevelVisitor visitor) throws Exception {
    visitor.externalIdRef(name, externalId, uri, encoding, getContents());
  }

}
