package com.thaiopensource.datatype.xsd;

import com.thaiopensource.datatype.DatatypeContext;

abstract class ValueRestrictDatatype extends RestrictDatatype {
  ValueRestrictDatatype(DatatypeBase base) {
    super(base);
  }

  boolean allowsValue(String str, DatatypeContext dc) {
    return getValue(str, dc) != null;
  }

  Object getValue(String str, DatatypeContext dc) {
    Object obj = base.getValue(str, dc);
    if (obj == null || !satisfiesRestriction(obj))
      return null;
    return obj;
  }

  abstract boolean satisfiesRestriction(Object value);
}
