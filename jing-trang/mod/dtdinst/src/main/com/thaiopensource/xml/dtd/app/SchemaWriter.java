package com.thaiopensource.xml.dtd.app;

import java.io.IOException;

import com.thaiopensource.xml.dtd.om.*;
import com.thaiopensource.xml.out.XmlWriter;
import com.thaiopensource.xml.em.ExternalId;

public class SchemaWriter implements TopLevelVisitor,
				     ModelGroupVisitor,
				     AttributeGroupVisitor,
				     DatatypeVisitor,
				     EnumGroupVisitor,
                                     FlagVisitor,
                                     NameSpecVisitor,
                                     AttributeDefaultVisitor {
  private final XmlWriter w;
  
  public SchemaWriter(XmlWriter writer) {
    this.w = writer;
  }

  public void writeDtd(Dtd dtd) throws IOException {
    String enc = dtd.getEncoding();
    if (enc != null)
      w.writeXmlDecl(enc);
    w.startElement("doctype");
    try {
      dtd.accept(this);
    }
    catch (RuntimeException e) {
      throw e;
    }
    catch (Exception e) {
      throw (IOException)e;
    }
    w.endElement();
  }

  public void elementDecl(NameSpec nameSpec, ModelGroup modelGroup)
    throws Exception {
    w.startElement("element");
    nameSpec.accept(this);
    modelGroup.accept(this);
    w.endElement();
  }

  public void attlistDecl(NameSpec nameSpec, AttributeGroup attributeGroup)
    throws Exception {
    w.startElement("attlist");
    nameSpec.accept(this);
    attributeGroup.accept(this);
    w.endElement();
  }

  public void processingInstruction(String target, String value) throws Exception {
    w.startElement("processingInstruction");
    w.attribute("target", target);
    w.characters(value);
    w.endElement();
  }

  public void comment(String value) throws Exception {
    w.startElement("comment");
    w.characters(value);
    w.endElement();
  }

  public void modelGroupDef(String name, ModelGroup modelGroup) throws Exception {
    w.startElement("modelGroup");
    w.attribute("name", name);
    modelGroup.accept(this);
    w.endElement();
  }

  public void attributeGroupDef(String name, AttributeGroup attributeGroup)
    throws Exception {
    w.startElement("attributeGroup");
    w.attribute("name", name);
    attributeGroup.accept(this);
    w.endElement();
  }

  public void enumGroupDef(String name, EnumGroup enumGroup) throws Exception {
    w.startElement("enumGroup");
    w.attribute("name", name);
    enumGroup.accept(this);
    w.endElement();
  }
  
  public void datatypeDef(String name, Datatype datatype) throws Exception {
    w.startElement("datatype");
    w.attribute("name", name);
    datatype.accept(this);
    w.endElement();
  }

  public void flagDef(String name, Flag flag) throws Exception {
    w.startElement("flag");
    w.attribute("name", name);
    flag.accept(this);
    w.endElement();
  }

  public void attributeDefaultDef(String name, AttributeDefault attributeDefault)
    throws Exception {
    w.startElement("attributeDefault");
    w.attribute("name", name);
    attributeDefault.accept(this);
    w.endElement();
  }

  public void choice(ModelGroup[] members) throws Exception {
    w.startElement("choice");
    for (int i = 0; i < members.length; i++)
      members[i].accept(this);
    w.endElement();
  }

  public void sequence(ModelGroup[] members) throws Exception {
    w.startElement("sequence");
    for (int i = 0; i < members.length; i++)
      members[i].accept(this);
    w.endElement();
  }

  public void oneOrMore(ModelGroup member) throws Exception {
    w.startElement("oneOrMore");
    member.accept(this);
    w.endElement();
  }

  public void zeroOrMore(ModelGroup member) throws Exception {
    w.startElement("zeroOrMore");
    member.accept(this);
    w.endElement();
  }

  public void optional(ModelGroup member) throws Exception {
    w.startElement("optional");
    member.accept(this);
    w.endElement();
  }

  public void modelGroupRef(String name, ModelGroup modelGroup) throws Exception {
    w.startElement("modelGroupRef");
    w.attribute("name", name);
    w.endElement();
  }

  public void elementRef(NameSpec nameSpec) throws Exception {
    w.startElement("elementRef");
    nameSpec.accept(this);
    w.endElement();
  }

  public void pcdata() throws Exception {
    w.startElement("pcdata");
    w.endElement();
  }

  public void any() throws Exception {
    w.startElement("any");
    w.endElement();
  }

  public void attribute(NameSpec nameSpec,
			Datatype datatype,
			AttributeDefault attributeDefault)
    throws Exception {
    w.startElement("attribute");
    nameSpec.accept(this);
    datatype.accept(this);
    attributeDefault.accept(this);
    w.endElement();
  }

  public void attributeGroupRef(String name, AttributeGroup attributeGroup)
    throws Exception {
    w.startElement("attributeGroupRef");
    w.attribute("name", name);
    w.endElement();
  }

  public void enumValue(String value) throws Exception {
    w.startElement("enum");
    w.characters(value);
    w.endElement();
  }

  public void enumGroupRef(String name, EnumGroup enumGroup) throws Exception {
    w.startElement("enumGroupRef");
    w.attribute("name", name);
    w.endElement();
  }

  public void cdataDatatype() throws IOException {
    w.startElement("cdata");
    w.endElement();
  }

  public void tokenizedDatatype(String typeName) throws IOException {
    w.startElement("tokenized");
    w.attribute("name", typeName);
    w.endElement();
  }

  public void enumDatatype(EnumGroup enumGroup) throws Exception {
    w.startElement("tokenized");
    enumGroup.accept(this);
    w.endElement();
  }

  public void notationDatatype(EnumGroup enumGroup) throws Exception {
    w.startElement("tokenized");
    w.attribute("name", "NOTATION");
    enumGroup.accept(this);
    w.endElement();
  }

  public void datatypeRef(String name, Datatype datatype) throws IOException {
    w.startElement("datatypeRef");
    w.attribute("name", name);
    w.endElement();
  }

  public void flagRef(String name, Flag flag) throws IOException {
    w.startElement("flagRef");
    w.attribute("name", name);
    w.endElement();
  }

  public void include() throws IOException {
    w.startElement("include");
    w.endElement();
  }

  public void ignore() throws IOException {
    w.startElement("ignore");
    w.endElement();
  }

  public void includedSection(Flag flag, TopLevel[] contents) throws Exception {
    w.startElement("includedSection");
    if (flag instanceof FlagRef)
      w.attribute("flag", ((FlagRef)flag).getName());
    for (int i = 0; i < contents.length; i++)
      contents[i].accept(this);
    w.endElement();
  }

  public void ignoredSection(Flag flag, String contents) throws Exception {
    w.startElement("ignoredSection");
    if (flag instanceof FlagRef)
      w.attribute("flag", ((FlagRef)flag).getName());
    w.characters(contents);
    w.endElement();
  }

  public void externalIdDef(String name, ExternalId xid) throws IOException {
    w.startElement("externalId");
    w.attribute("name", name);
    externalId(xid);
    w.endElement();
  }

  public void externalIdRef(String name, ExternalId xid, String uri,
			    String encoding, TopLevel[] contents)
    throws Exception {
    w.startElement("externalIdRef");
    w.attribute("name", name);
    for (int i = 0; i < contents.length; i++)
      contents[i].accept(this);
    w.endElement();
  }

  public void internalEntityDecl(String name, String value) throws Exception {
    w.startElement("internalEntity");
    w.attribute("name", name);
    boolean useCharRef = value.length() == 1 && value.charAt(0) >= 0x80;
    w.characters(value, useCharRef);
    w.endElement();
  }

  public void externalEntityDecl(String name, ExternalId xid) throws IOException {
    w.startElement("externalEntity");
    w.attribute("name", name);
    externalId(xid);
    w.endElement();
  }

  public void notationDecl(String name, ExternalId xid) throws IOException {
    w.startElement("notation");
    w.attribute("name", name);
    externalId(xid);
    w.endElement();
  }

  private void externalId(ExternalId xid) throws IOException {
    attributeIfNotNull("system", xid.getSystemId());
    attributeIfNotNull("public", xid.getPublicId());
    // this messes up testing
    // attributeIfNotNull("xml:base", xid.getBaseUri());
  }

  private void attributeIfNotNull(String name, String value)
    throws IOException {
    if (value != null)
      w.attribute(name, value);
  }

  public void nameSpecDef(String name, NameSpec nameSpec) throws Exception {
    w.startElement("nameSpec");
    w.attribute("name", name);
    nameSpec.accept(this);
    w.endElement();
  }

  public void name(String value) throws IOException {
    w.startElement("name");
    w.characters(value);
    w.endElement();
  }

  public void nameSpecRef(String name, NameSpec nameSpec) throws Exception {
    w.startElement("nameSpecRef");
    w.attribute("name", name);
    w.endElement();
  }

  public void overriddenDef(Def def, boolean duplicate) throws Exception {
    w.startElement("overridden");
    if (duplicate) {
      w.startElement("duplicate");
      w.attribute("name", def.getName());
      w.endElement();
    }
    else
      def.accept(this);
    w.endElement();
  }

  public void paramDef(String name, String value) throws IOException {
    w.startElement("param");
    w.attribute("name", name);
    w.characters(value);
    w.endElement();
  }

  public void defaultValue(String value) throws Exception {
    w.startElement("default");
    w.characters(value);
    w.endElement();
  }

  public void fixedValue(String value) throws Exception {
    w.startElement("fixed");
    w.characters(value);
    w.endElement();
  }

  public void impliedValue() throws Exception {
    w.startElement("implied");
    w.endElement();
  }

  public void requiredValue() throws Exception {
    w.startElement("required");
    w.endElement();
  }

  public void attributeDefaultRef(String name,
				  AttributeDefault attributeDefault)
    throws Exception {
    w.startElement("attributeDefaultRef");
    w.attribute("name", name);
    w.endElement();
  }
}
