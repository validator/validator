package com.thaiopensource.datatype.xsd;

import org.relaxng.datatype.ValidationContext;

class MaxInclusiveRestrictDatatype extends ValueRestrictDatatype {
  private OrderRelation order;
  private Object limit;

  MaxInclusiveRestrictDatatype(DatatypeBase base, Object limit) {
    super(base);
    this.order = base.getOrderRelation();
    this.limit = limit;
  }

  boolean satisfiesRestriction(Object value) {
    return order.compareValue(limit, value) >= 0;
  }
}
