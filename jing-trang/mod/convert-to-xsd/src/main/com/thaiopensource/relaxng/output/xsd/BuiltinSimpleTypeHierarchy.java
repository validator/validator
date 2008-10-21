package com.thaiopensource.relaxng.output.xsd;

class BuiltinSimpleTypeHierarchy {
  private BuiltinSimpleTypeHierarchy() {
  }
  static private final String[] parentType = {
    "normalizedString", "string",
    "token", "normalizedString",
    "language", "token",
    "Name", "token",
    "NMTOKEN", "token",
    "NCName", "Name",
    "ID", "NCName",
    "IDREF", "NCName",
    "ENTITY", "NCName",
    "integer", "decimal",
    "nonPositiveInteger", "integer",
    "long", "integer",
    "nonNegativeInteger", "integer",
    "negativeInteger", "nonPositiveInteger",
    "positiveInteger", "nonNegativeInteger",
    "int", "long",
    "unsignedLong", "nonNegativeInteger",
    "short", "int",
    "byte", "short",
    "unsignedInt", "unsignedLong",
    "unsignedShort", "unsignedInt",
    "unsignedByte", "unsignedShort"
  };

  static String getParentType(String type) {
    for (int i = 0; i < parentType.length; i += 2)
      if (type.equals(parentType[i]))
        return parentType[i + 1];
    return null;
  }
}
