package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.xml.util.Name;

public interface Structure {
  Name getName();
  <T> T accept(StructureVisitor<T> visitor);
}
