package com.thaiopensource.relaxng;

class AttributeNameClassChecker implements NameClassVisitor {
  private static final String XMLNS_URI = "http://www.w3.org/2000/xmlns";
  private String errorMessageId = null;
  
  public void visitChoice(NameClass nc1, NameClass nc2) {
    nc1.accept(this);
    nc2.accept(this);
  }

  public void visitNsName(String ns) {
    if (ns.equals(XMLNS_URI))
      errorMessageId = "xmlns_uri_attribute";
  }

  public void visitNsNameExcept(String ns, NameClass nc) {
    visitNsName(ns);
    nc.accept(this);
  }

  public void visitAnyName() { }

  public void visitAnyNameExcept(NameClass nc) {
    nc.accept(this);
  }

  public void visitName(Name name) {
    visitNsName(name.getNamespaceUri());
    if (name.equals(new Name("", "xmlns")))
      errorMessageId = "xmlns_attribute";
  }

  public void visitNull() { }

  public void visitError() { }

  String checkNameClass(NameClass nc) {
    errorMessageId = null;
    nc.accept(this);
    return errorMessageId;
  }
}
