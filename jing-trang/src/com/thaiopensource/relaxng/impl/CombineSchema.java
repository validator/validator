package com.thaiopensource.relaxng.impl;

import com.thaiopensource.validate.AbstractSchema;
import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.ValidatorHandler;
import org.xml.sax.ErrorHandler;

public class CombineSchema extends AbstractSchema {
  private final Schema schema1;
  private final Schema schema2;

  public CombineSchema(Schema schema1, Schema schema2) {
    this.schema1 = schema1;
    this.schema2 = schema2;
  }

  public ValidatorHandler createValidator(ErrorHandler eh) {
    return new CombineValidatorHandler(schema1.createValidator(eh),
                                       schema2.createValidator(eh));
  }
}
