package com.thaiopensource.xml.dtd;

public interface TopLevelVisitor {
  void elementDecl(String name, ModelGroup modelGroup)
    throws Exception;
  void attlistDecl(String name, AttributeGroup attributeGroup)
    throws Exception;
  void processingInstruction(String target, String value) throws Exception;
  void comment(String value) throws Exception;
  void ignoredSection(String value) throws Exception;
  void modelGroupDef(String name, ModelGroup modelGroup) throws Exception;
  void attributeGroupDef(String name, AttributeGroup attributeGroup)
    throws Exception;
  void enumGroupDef(String name, EnumGroup enumGroup) throws Exception;
  void datatypeDef(String name, Datatype datatype) throws Exception;
  void flagDef(String name, Flag flag) throws Exception;
  void includeSection(Flag flag, TopLevel[] contents) throws Exception;
  void ignoreSection(Flag flag, String contents) throws Exception;
}

