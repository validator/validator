package com.thaiopensource.xml.dtd.om;

import com.thaiopensource.xml.em.ExternalId;

public interface TopLevelVisitor {
  void elementDecl(NameSpec nameSpec, ModelGroup modelGroup)
    throws Exception;
  void attlistDecl(NameSpec nameSpec, AttributeGroup attributeGroup)
    throws Exception;
  void processingInstruction(String target, String value) throws Exception;
  void comment(String value) throws Exception;
  void modelGroupDef(String name, ModelGroup modelGroup) throws Exception;
  void attributeGroupDef(String name, AttributeGroup attributeGroup)
    throws Exception;
  void enumGroupDef(String name, EnumGroup enumGroup) throws Exception;
  void datatypeDef(String name, Datatype datatype) throws Exception;
  void flagDef(String name, Flag flag) throws Exception;
  void includedSection(Flag flag, TopLevel[] contents) throws Exception;
  void ignoredSection(Flag flag, String contents) throws Exception;
  void internalEntityDecl(String name, String value) throws Exception;
  void externalEntityDecl(String name, ExternalId externalId) throws Exception;
  void notationDecl(String name, ExternalId externalId) throws Exception;
  void nameSpecDef(String name, NameSpec nameSpec) throws Exception;
  void overriddenDef(Def def, boolean isDuplicate) throws Exception;
  void externalIdDef(String name, ExternalId externalId) throws Exception;
  void externalIdRef(String name, ExternalId externalId, String uri,
		     String encoding, TopLevel[] contents) throws Exception;
  void paramDef(String name, String value) throws Exception;
  void attributeDefaultDef(String name, AttributeDefault ad) throws Exception;
}
