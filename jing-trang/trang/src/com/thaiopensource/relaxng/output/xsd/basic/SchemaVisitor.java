package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.relaxng.edit.SourceLocation;

import java.util.List;

public interface SchemaVisitor {
  void visitGroup(GroupDefinition def);
  void visitAttributeGroup(AttributeGroupDefinition def);
  void visitSimpleType(SimpleTypeDefinition def);
  void visitRoot(RootDeclaration decl);
  void visitInclude(Include include);
  void visitComment(Comment comment);
}
