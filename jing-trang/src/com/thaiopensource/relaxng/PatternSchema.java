package com.thaiopensource.relaxng;

import org.xml.sax.ErrorHandler;

class PatternSchema extends AbstractSchema {
  private final SchemaPatternBuilder spb;
  private final Pattern start;

  PatternSchema(SchemaPatternBuilder spb, Pattern start) {
    this.spb = spb;
    this.start = start;
  }

  public ValidatorHandler createValidator(ErrorHandler eh) {
    return new PatternValidatorHandler(start, new ValidatorPatternBuilder(spb), eh);
  }
}
