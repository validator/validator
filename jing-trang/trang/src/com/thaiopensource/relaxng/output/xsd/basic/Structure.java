package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.relaxng.output.common.Name;

public interface Structure {
  Name getName();
  Object accept(StructureVisitor visitor);
}
