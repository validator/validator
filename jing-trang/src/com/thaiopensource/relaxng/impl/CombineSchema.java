package com.thaiopensource.relaxng.impl;

import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.ValidatorHandler;
import com.thaiopensource.util.PropertyMap;

public class CombineSchema implements Schema {
  private final Schema schema1;
  private final Schema schema2;

  public CombineSchema(Schema schema1, Schema schema2) {
    this.schema1 = schema1;
    this.schema2 = schema2;
  }

  public ValidatorHandler createValidator(PropertyMap properties) {
    return new CombineValidatorHandler(schema1.createValidator(properties),
                                       schema2.createValidator(properties));
  }
}
