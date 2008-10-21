package com.thaiopensource.relaxng.impl;

import com.thaiopensource.xml.util.Name;

class OverlapDetector implements NameClassVisitor {
  private final NameClass nc1;
  private final NameClass nc2;
  private Name overlapExample = null;

  private static final String IMPOSSIBLE = "\u0000";

  private OverlapDetector(NameClass nc1, NameClass nc2) {
    this.nc1 = nc1;
    this.nc2 = nc2;
    nc1.accept(this);
    nc2.accept(this);      
  }

  private void probe(Name name) {
    if (nc1.contains(name) && nc2.contains(name))
      overlapExample = name;
  }

  public void visitChoice(NameClass nc1, NameClass nc2) {
    nc1.accept(this);
    nc2.accept(this);
  }

  public void visitNsName(String ns) {
    probe(new Name(ns, IMPOSSIBLE));
  }

  public void visitNsNameExcept(String ns, NameClass ex) {
    probe(new Name(ns, IMPOSSIBLE));
    ex.accept(this);
  }

  public void visitAnyName() {
    probe(new Name(IMPOSSIBLE, IMPOSSIBLE));
  }

  public void visitAnyNameExcept(NameClass ex) {
    probe(new Name(IMPOSSIBLE, IMPOSSIBLE));
    ex.accept(this);
  }

  public void visitName(Name name) {
    probe(name);
  }

  public void visitNull() {
  }

  public void visitError() {
  }

  static void checkOverlap(NameClass nc1, NameClass nc2,
                           String messageForName,
                           String messageForNs,
                           String messageForOther) throws RestrictionViolationException {
    if (nc2 instanceof SimpleNameClass) {
      SimpleNameClass snc = (SimpleNameClass)nc2;
      if (nc1.contains(snc.getName()))
        throw new RestrictionViolationException(messageForName, snc.getName());
    }
    else if (nc1 instanceof SimpleNameClass) {
      SimpleNameClass snc = (SimpleNameClass)nc1;
      if (nc2.contains(snc.getName()))
        throw new RestrictionViolationException(messageForName, snc.getName());
    }
    else {
      Name name = new OverlapDetector(nc1, nc2).overlapExample;
      if (name != null) {
        String localName = name.getLocalName();
        if (localName == IMPOSSIBLE) {
          String ns = name.getNamespaceUri();
          if (ns == IMPOSSIBLE)
            throw new RestrictionViolationException(messageForOther);
          else
            throw new RestrictionViolationException(messageForNs, ns);
        }
        else
          throw new RestrictionViolationException(messageForName, name);
      }
    }
  }
}
