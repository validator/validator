package com.thaiopensource.validate.schematron;

import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.Validator;
import com.thaiopensource.util.PropertyMap;

import javax.xml.transform.Templates;

class SchemaImpl implements Schema {
  private final Templates templates;

  SchemaImpl(Templates templates) {
    this.templates = templates;
  }

  public Validator createValidator(PropertyMap properties) {
    return new ValidatorImpl(templates, properties);
  }
}
