package com.thaiopensource.datatype.xsd;

import com.thaiopensource.datatype.DatatypeContext;

class MinInclusiveRestrictDatatype extends ValueRestrictDatatype {
  private OrderRelation order;
  private Object limit;

  MinInclusiveRestrictDatatype(DatatypeBase base, Object limit) {
    super(base);
    this.order = base.getOrderRelation();
    this.limit = limit;
  }

  boolean satisfiesRestriction(Object value) {
    return order.compareValue(value, limit) >= 0;
  }
}
