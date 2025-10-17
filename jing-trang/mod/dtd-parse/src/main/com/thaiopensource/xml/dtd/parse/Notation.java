package com.thaiopensource.xml.dtd.parse;

import com.thaiopensource.xml.em.ExternalId;

class Notation {
  final String name;
  String systemId;
  String publicId;
  String baseUri;
  
  Notation(String name) {
    this.name = name;
  }

  ExternalId getExternalId() {
    return new ExternalId(systemId, publicId, baseUri);
  }
}
