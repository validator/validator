package com.thaiopensource.relaxng.edit;

import com.thaiopensource.relaxng.parse.ParsedNameClass;
import com.thaiopensource.relaxng.parse.SchemaBuilder;

public abstract class NameClass extends Annotated implements ParsedNameClass {
  public static final String INHERIT_NS = SchemaBuilder.INHERIT_NS;
  public abstract Object accept(NameClassVisitor visitor);
}
