package com.thaiopensource.datatype.xsd;

interface OrderRelation {
  static final int COMPARE_INCOMPARABLE = -2;
  static final int COMPARE_LESS_THAN = -1;
  static final int COMPARE_EQUAL = 0;
  static final int COMPARE_GREATER_THAN = 1;

  int compareValue(Object obj1, Object obj2);
}
