package com.thaiopensource.relaxng;

class OverlapDetector implements NameClassVisitor {
  private NameClass nc1;
  private NameClass nc2;
  private boolean overlaps = false;

  static final String IMPOSSIBLE = "\u0000";

  private OverlapDetector(NameClass nc1, NameClass nc2) {
    this.nc1 = nc1;
    this.nc2 = nc2;
    nc1.accept(this);
    nc2.accept(this);      
  }

  private void probe(String namespaceURI, String localName) {
    if (nc1.contains(namespaceURI, localName)
	&& nc2.contains(namespaceURI, localName))
      overlaps = true;
  }

  public void visitChoice(NameClass nc1, NameClass nc2) {
    nc1.accept(this);
    nc2.accept(this);
  }

  public void visitNsName(String ns) {
    probe(ns, IMPOSSIBLE);
  }

  public void visitNsNameExcept(String ns, NameClass ex) {
    probe(ns, IMPOSSIBLE);
    ex.accept(this);
  }

  public void visitAnyName() {
    probe(IMPOSSIBLE, IMPOSSIBLE);
  }

  public void visitAnyNameExcept(NameClass ex) {
    probe(IMPOSSIBLE, IMPOSSIBLE);
    ex.accept(this);
  }

  public void visitName(String ns, String localName) {
    probe(ns, localName);
  }

  public void visitNull() {
  }

  public void visitError() {
  }

  static boolean overlap(NameClass nc1, NameClass nc2) {
    if (nc2 instanceof SimpleNameClass) {
      SimpleNameClass snc = (SimpleNameClass)nc2;
      return nc1.contains(snc.getNamespaceURI(), snc.getLocalName());
    }
    if (nc1 instanceof SimpleNameClass) {
      SimpleNameClass snc = (SimpleNameClass)nc1;
      return nc2.contains(snc.getNamespaceURI(), snc.getLocalName());
    }
    return new OverlapDetector(nc1, nc2).overlaps;
  }
}
