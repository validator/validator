package com.thaiopensource.relaxng;

import com.thaiopensource.datatype.Datatype;
import com.thaiopensource.datatype.DatatypeContext;
import com.thaiopensource.datatype.DatatypeBuilder;
import com.thaiopensource.datatype.InvalidParamException;

class BuiltinDatatypeBuilder implements DatatypeBuilder {
  private final Datatype dt;

  BuiltinDatatypeBuilder(Datatype dt) {
    this.dt = dt;
  }

  public void addParam(String name,
		       String value,
		       DatatypeContext context) throws InvalidParamException {
    throw new InvalidParamException(Localizer.message("builtin_param"));
  }

  public Datatype finish() {
    return dt;
  }
}
