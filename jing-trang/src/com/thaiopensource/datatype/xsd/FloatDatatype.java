package com.thaiopensource.datatype.xsd;

import com.thaiopensource.datatype.DatatypeContext;

class FloatDatatype extends DoubleDatatype {

  Object getValue(String str, DatatypeContext dc) {
    if (str.equals("INF"))
      return new Float(Float.POSITIVE_INFINITY);
    if (str.equals("-INF"))
      return new Float(Float.NEGATIVE_INFINITY);
    if (str.equals("NaN"))
      return new Float(Float.NaN);
    return new Float(str);
  }

  public int compareValue(Object obj1, Object obj2) {
    float f1 = ((Float)obj1).floatValue();
    float f2 = ((Float)obj2).floatValue();
    if (f1 < f2)
      return COMPARE_LESS_THAN;
    if (f1 > f2)
      return COMPARE_GREATER_THAN;
    int bits1 = Float.floatToIntBits(f1);
    int bits2 = Float.floatToIntBits(f2);
    if (bits1 == bits2)
      return COMPARE_EQUAL;
    if (Float.isNaN(f1) || Float.isNaN(f2))
      return COMPARE_INCOMPARABLE;
    // Must be +0 and -0
    return bits1 < bits2 ? COMPARE_LESS_THAN : COMPARE_GREATER_THAN;
  }
}
