package com.thaiopensource.validate;

import com.thaiopensource.validate.Flag;
import com.thaiopensource.util.PropertyId;
import com.thaiopensource.util.PropertyMapBuilder;

public class FlagPropertyId extends PropertyId {
  public FlagPropertyId(String name) {
    super(name, Flag.class);
  }

  public void add(PropertyMapBuilder builder) {
    builder.put(this, Flag.PRESENT);
  }
}
