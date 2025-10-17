package com.thaiopensource.xml.dtd.parse;

import java.util.Vector;

class AtomStream {
  int tokenType;
  String token;
  Entity entity;
  boolean eof;

  private int i;
  private final int len;
  private final Vector v;

  AtomStream(Vector v) {
    this.v = v;
    this.i = 0;
    this.len = v.size();
  }

  boolean advance() {
    if (i >= len) {
      eof = true;
      token = null;
      entity = null;
      tokenType = -1;
      return false;
    }
    Atom a = (Atom)v.elementAt(i);
    token = a.getToken();
    tokenType = a.getTokenType();
    entity = a.getEntity();
    i++;
    return true;
  }
}
