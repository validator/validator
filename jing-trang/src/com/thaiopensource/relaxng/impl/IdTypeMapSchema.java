package com.thaiopensource.relaxng.impl;

import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.Validator;
import org.xml.sax.ErrorHandler;

public class IdTypeMapSchema implements Schema {
  private final IdTypeMap idTypeMap;

  public IdTypeMapSchema(IdTypeMap idTypeMap) {
    this.idTypeMap = idTypeMap;
  }

  public Validator createValidator(PropertyMap properties) {
    ErrorHandler eh = ValidateProperty.ERROR_HANDLER.get(properties);
    return new IdSoundnessChecker(idTypeMap, eh);
  }
}
