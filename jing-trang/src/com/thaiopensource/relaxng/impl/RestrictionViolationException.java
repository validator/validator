package com.thaiopensource.relaxng.impl;

import org.xml.sax.Locator;

class RestrictionViolationException extends Exception {
  private String messageId;
  private Locator loc;

  RestrictionViolationException(String messageId) {
    this.messageId = messageId;
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
}
  
