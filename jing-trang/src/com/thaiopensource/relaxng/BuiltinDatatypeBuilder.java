package com.thaiopensource.relaxng;

import org.relaxng.datatype.Datatype;
import org.relaxng.datatype.ValidationContext;
import org.relaxng.datatype.DatatypeBuilder;
import org.relaxng.datatype.DatatypeException;

class BuiltinDatatypeBuilder implements DatatypeBuilder {
  private final Datatype dt;

  BuiltinDatatypeBuilder(Datatype dt) {
    this.dt = dt;
  }

  public void addParameter(String name,
			   String value,
			   ValidationContext context) throws DatatypeException {
    throw new DatatypeException(Localizer.message("builtin_param"));
  }

  public Datatype createDatatype() {
    return dt;
  }
}
