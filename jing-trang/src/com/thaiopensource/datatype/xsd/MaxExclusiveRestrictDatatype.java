package com.thaiopensource.datatype.xsd;

import org.relaxng.datatype.ValidationContext;

class MaxExclusiveRestrictDatatype extends ValueRestrictDatatype {
  private OrderRelation order;
  private Object limit;

  MaxExclusiveRestrictDatatype(DatatypeBase base, Object limit) {
    super(base);
    this.order = base.getOrderRelation();
    this.limit = limit;
  }

  boolean satisfiesRestriction(Object value) {
    return order.isLessThan(value, limit);
  }
}
