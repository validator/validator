package com.thaiopensource.relaxng.mns;

import com.thaiopensource.relaxng.Schema;
import com.thaiopensource.relaxng.ValidatorHandler;

import java.util.Map;

import org.xml.sax.ErrorHandler;

class SchemaImpl implements Schema {
  private final Map namespaceMap;

  SchemaImpl(Map namespaceMap) {
    this.namespaceMap = namespaceMap;
  }

  public ValidatorHandler createValidator(ErrorHandler eh) {
    return new ValidatorHandlerImpl(namespaceMap, eh);
  }

  public ValidatorHandler createValidator() {
    return createValidator(null);
  }
}
