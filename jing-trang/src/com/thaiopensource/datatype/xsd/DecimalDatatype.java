package com.thaiopensource.datatype.xsd;

import java.math.BigDecimal;

import com.thaiopensource.datatype.DatatypeContext;

class DecimalDatatype extends DatatypeBase implements OrderRelation {

  boolean lexicallyAllows(String str) {
    int len = str.length();
    if (len == 0)
      return false;
    int i = 0;
    switch (str.charAt(i)) {
    case '+':
    case '-':
      if (++i == len)
	return false;
    }
    boolean hadDecimalPoint = false;
    if (str.charAt(i) == '.') {
      hadDecimalPoint = true;
      if (++i == len)
	return false;
    }
    do {
      switch (str.charAt(i)) {
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
	break;
      case '.':
	if (hadDecimalPoint)
	  return false;
	hadDecimalPoint = true;
	break;
      default:
	return false;
      }
    } while (++i < len);
    return true;
  }

  /* BigDecimal.equals considers objects distinct if they have the
     different scales but the same mathematical value. */

  static class Decimal extends BigDecimal {
    Decimal(String str) {
      super(str);
    }
    public boolean equals(Object obj) {
      if (obj == null || !(obj instanceof Decimal))
	return false;
      return compareTo((Decimal)obj) == 0;
    }
  }

  Object getValue(String str, DatatypeContext dc) {
    if (str.charAt(0) == '+')
      str = str.substring(1);	// JDK 1.1 doesn't handle leading +
    return new Decimal(str);
  }

  OrderRelation getOrderRelation() {
    return this;
  }

  public int compareValue(Object obj1, Object obj2) {
    return ((BigDecimal)obj1).compareTo((BigDecimal)obj2);
  }

}
