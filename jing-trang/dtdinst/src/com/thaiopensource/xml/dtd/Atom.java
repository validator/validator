package com.thaiopensource.xml.dtd;

class Atom {
  private int tokenType;
  private String token;
  private Entity entity;

  Atom(Entity entity) {
    this.entity = entity;
    this.tokenType = -1;
    this.token = null;
  }

  Atom(int tokenType, String token) {
    this.tokenType = tokenType;
    this.token = token;
  }

  final int getTokenType() {
    return tokenType;
  }

  final String getToken() {
    return token;
  }

  final Entity getEntity() {
    return entity;
  }

  void setEntity(Entity entity) {
    this.entity = entity;
  }

  public int hashCode() {
    return token.hashCode();
  }

  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof Atom))
      return false;
    Atom other = (Atom)obj;
    if (this.entity != null)
      return this.entity == other.entity;
    else
      return this.tokenType == other.tokenType && this.token.equals(other.token);
  }
}

