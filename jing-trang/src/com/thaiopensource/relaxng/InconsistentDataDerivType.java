package com.thaiopensource.relaxng;

import org.relaxng.datatype.ValidationContext;

class InconsistentDataDerivType extends DataDerivType {
  static private final InconsistentDataDerivType instance = new InconsistentDataDerivType();

  static InconsistentDataDerivType getInstance() {
    return instance;
  }

  private InconsistentDataDerivType() { }

  DataDerivType combine(DataDerivType ddt) {
    return this;
  }

  DataDerivType copy() {
    return this;
  }
}
