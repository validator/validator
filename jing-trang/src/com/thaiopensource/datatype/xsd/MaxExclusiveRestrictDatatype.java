package com.thaiopensource.datatype.xsd;

import com.thaiopensource.datatype.DatatypeContext;

class MaxExclusiveRestrictDatatype extends ValueRestrictDatatype {
  private OrderRelation order;
  private Object limit;

  MaxExclusiveRestrictDatatype(DatatypeBase base, Object limit) {
    super(base);
    this.order = base.getOrderRelation();
    this.limit = limit;
  }

  boolean satisfiesRestriction(Object value) {
    return order.compareValue(limit, value) > 0;
  }
}
