package com.thaiopensource.datatype.xsd;

class EnumerationRestrictDatatype extends ValueRestrictDatatype {
  private Object[] values;

  EnumerationRestrictDatatype(DatatypeBase base, Object[] values) {
    super(base);
    this.values = values;
  }

  boolean satisfiesRestriction(Object obj) {
    for (int i = 0; i < values.length; i++)
      if (obj.equals(values[i]))
	return true;
    return false;
  }
}
