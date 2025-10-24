package com.thaiopensource.validate.schematron;

import com.thaiopensource.util.PropertyId;
import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.validate.AbstractSchema;
import com.thaiopensource.validate.Validator;

import javax.xml.transform.Templates;
import javax.xml.transform.sax.SAXTransformerFactory;

class SchemaImpl extends AbstractSchema {
  private final Templates templates;
  private final Class factoryClass;

  SchemaImpl(Templates templates, Class factoryClass, PropertyMap properties, PropertyId[] supportedPropertyIds) {
    super(properties, supportedPropertyIds);
    this.templates = templates;
    this.factoryClass = factoryClass;
  }

  public Validator createValidator(PropertyMap properties) {
    try {
      return new ValidatorImpl(templates,
                               (SAXTransformerFactory)factoryClass.newInstance(),
                               properties);
    }
    catch (InstantiationException e) {
      throw new RuntimeException("unexpected InstantiationException creating SAXTransformerFactory");
    }
    catch (IllegalAccessException e) {
      throw new RuntimeException("unexpected IllegalAccessException creating SAXTransformerFactory");
    }
  }
}
