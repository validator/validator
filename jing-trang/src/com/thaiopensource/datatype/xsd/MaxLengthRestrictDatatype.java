package com.thaiopensource.datatype.xsd;

class MaxLengthRestrictDatatype extends ValueRestrictDatatype {
  private int length;
  private Measure measure;

  MaxLengthRestrictDatatype(DatatypeBase base, int length) {
    super(base);
    this.measure = base.getMeasure();
    this.length = length;
  }

  boolean satisfiesRestriction(Object obj) {
    return measure.getLength(obj) <= length;
  }
}
