package com.thaiopensource.relaxng.impl;

import com.thaiopensource.validate.AbstractSchema;
import com.thaiopensource.validate.ValidatorHandler;
import org.xml.sax.ErrorHandler;

public class FeasibleIdTypeMapSchema extends AbstractSchema {
  private final IdTypeMap idTypeMap;

  public FeasibleIdTypeMapSchema(IdTypeMap idTypeMap) {
    this.idTypeMap = idTypeMap;
  }

  public ValidatorHandler createValidator(ErrorHandler eh) {
    return new FeasibleIdSoundnessChecker(idTypeMap, eh);
  }
}
