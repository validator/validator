package com.thaiopensource.relaxng.edit;

public class ExternalRefPattern extends Pattern {
  private String href;
  private String ns;
  private String baseUri;

  public ExternalRefPattern(String href) {
    this.href = href;
  }

  public String getHref() {
    return href;
  }

  public void setHref(String href) {
    this.href = href;
  }

  public String getNs() {
    return ns;
  }

  public void setNs(String ns) {
    this.ns = ns;
  }

  public String getBaseUri() {
    return baseUri;
  }

  public void setBaseUri(String baseUri) {
    this.baseUri = baseUri;
  }

  public Object accept(PatternVisitor visitor) {
    return visitor.visitExternalRef(this);
  }
}
