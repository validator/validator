package com.thaiopensource.relaxng.edit;

public class ExternalRefPattern extends Pattern {
  private String uri;
  private String ns;
  private String href;
  private String baseUri;

  public ExternalRefPattern(String uri) {
    this.uri = uri;
  }

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  public String getNs() {
    return ns;
  }

  public void setNs(String ns) {
    this.ns = ns;
  }

  public String getHref() {
    return href;
  }

  public void setHref(String href) {
    this.href = href;
  }

  public String getBaseUri() {
    return baseUri;
  }

  public void setBaseUri(String baseUri) {
    this.baseUri = baseUri;
  }

  public <T> T accept(PatternVisitor<T> visitor) {
    return visitor.visitExternalRef(this);
  }
}
