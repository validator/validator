package com.thaiopensource.validate.picl;

import com.thaiopensource.validate.AbstractSchema;
import com.thaiopensource.validate.Validator;
import com.thaiopensource.util.PropertyMap;

class SchemaImpl extends AbstractSchema {
  private final Constraint constraint;

  SchemaImpl(PropertyMap properties, Constraint constraint) {
    super(properties);
    this.constraint = constraint;
  }

  public Validator createValidator(PropertyMap properties) {
    return new ValidatorImpl(constraint, properties);
  }

}
