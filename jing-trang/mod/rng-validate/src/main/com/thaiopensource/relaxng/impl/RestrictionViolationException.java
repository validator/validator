package com.thaiopensource.relaxng.impl;

import org.xml.sax.Locator;
import com.thaiopensource.xml.util.Name;

class RestrictionViolationException extends Exception {
  private final String messageId;
  private Locator loc;
  private Name name;
  private String namespaceUri;

  RestrictionViolationException(String messageId) {
    this.messageId = messageId;
  }

  RestrictionViolationException(String messageId, Name name) {
    this.messageId = messageId;
    this.name = name;
  }

  RestrictionViolationException(String messageId, String namespaceUri) {
    this.messageId = messageId;
    this.namespaceUri = namespaceUri;
  }

  String getMessageId() {
    return messageId;
  }

  Locator getLocator() {
    return loc;
  }

  void maybeSetLocator(Locator loc) {
    if (this.loc == null)
      this.loc = loc;
  }

  Name getName() {
    return name;
  }

  String getNamespaceUri() {
    return namespaceUri;
  }
}
  
