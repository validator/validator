package com.thaiopensource.xml.em;

public final class ExternalId {
  private final String systemId;
  private final String publicId;
  private final String baseUri;

  public ExternalId(String systemId, String publicId, String baseUri) {
    this.systemId = systemId;
    this.publicId = publicId;
    this.baseUri = baseUri;
  }

  public ExternalId(String systemId) {
    this(systemId, null, null);
  }

  public String getSystemId() {
    return systemId;
  }

  public String getPublicId() {
    return publicId;
  }

  public String getBaseUri() {
    return baseUri;
  }
}
