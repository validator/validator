package com.thaiopensource.relaxng;

import org.xml.sax.ErrorHandler;

class IdTypeMapSchema extends AbstractSchema {
  private final IdTypeMap idTypeMap;

  IdTypeMapSchema(IdTypeMap idTypeMap) {
    this.idTypeMap = idTypeMap;
  }

  public ValidatorHandler createValidator(ErrorHandler eh) {
    return new IdSoundnessChecker(idTypeMap, eh);
  }
}
