package com.thaiopensource.datatype.xsd;

import org.relaxng.datatype.ValidationContext;

class DoubleDatatype extends DatatypeBase implements OrderRelation {

  boolean lexicallyAllows(String str) {
    if (str.equals("INF") || str.equals("-INF") || str.equals("NaN"))
      return true;
    int len = str.length();
    boolean hadSign = false;
    boolean hadDecimalPoint = false;
    boolean hadDigit = false;
    boolean hadE = false;
    for (int i = 0; i < len; i++) {
      switch (str.charAt(i)) {
      case '+':
      case '-':
	if (hadDigit || hadDecimalPoint || hadSign)
	  return false;
	hadSign = true;
	break;
      case '0':
      case '1':
      case '2':
      case '3':
      case '4':
      case '5':
      case '6':
      case '7':
      case '8':
      case '9':
	hadDigit = true;
	break;
      case 'e':
      case 'E':
	if (hadE || !hadDigit)
	  return false;
	hadDigit = false;
	hadE = true;
	hadSign = false;
	hadDecimalPoint = false;
	break;
      case '.':
	if (hadDecimalPoint || hadE)
	  return false;
	hadDecimalPoint = true;
	break;
      default:
	return false;
      }
    }
    return hadDigit;
  }

  Object getValue(String str, ValidationContext vc) {
    if (str.equals("INF"))
      return new Double(Double.POSITIVE_INFINITY);
    if (str.equals("-INF"))
      return new Double(Double.NEGATIVE_INFINITY);
    if (str.equals("NaN"))
      return new Double(Double.NaN);
    return new Double(str);
  }

  OrderRelation getOrderRelation() {
    return this;
  }

  public int compareValue(Object obj1, Object obj2) {
    double d1 = ((Double)obj1).doubleValue();
    double d2 = ((Double)obj2).doubleValue();
    if (d1 < d2)
      return COMPARE_LESS_THAN;
    if (d1 > d2)
      return COMPARE_GREATER_THAN;
    long bits1 = Double.doubleToLongBits(d1);
    long bits2 = Double.doubleToLongBits(d2);
    if (bits1 == bits2)
      return COMPARE_EQUAL;
    if (Double.isNaN(d1) || Double.isNaN(d2))
      return COMPARE_INCOMPARABLE;
    // Must be +0 and -0
    return bits1 < bits2 ? COMPARE_LESS_THAN : COMPARE_GREATER_THAN;
  }
}
