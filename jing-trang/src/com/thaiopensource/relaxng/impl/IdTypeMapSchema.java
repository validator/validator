package com.thaiopensource.relaxng.impl;

import com.thaiopensource.relaxng.AbstractSchema;
import com.thaiopensource.relaxng.ValidatorHandler;
import org.xml.sax.ErrorHandler;

public class IdTypeMapSchema extends AbstractSchema {
  private final IdTypeMap idTypeMap;

  public IdTypeMapSchema(IdTypeMap idTypeMap) {
    this.idTypeMap = idTypeMap;
  }

  public ValidatorHandler createValidator(ErrorHandler eh) {
    return new IdSoundnessChecker(idTypeMap, eh);
  }
}
