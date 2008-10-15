package com.thaiopensource.relaxng.output.xsd.basic;

public interface SchemaVisitor {
  void visitGroup(GroupDefinition def);
  void visitAttributeGroup(AttributeGroupDefinition def);
  void visitSimpleType(SimpleTypeDefinition def);
  void visitRoot(RootDeclaration decl);
  void visitInclude(Include include);
  void visitComment(Comment comment);
}
