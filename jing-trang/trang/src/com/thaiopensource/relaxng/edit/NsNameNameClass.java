package com.thaiopensource.relaxng.edit;

public class NsNameNameClass extends OpenNameClass {
  private String ns;

  public NsNameNameClass(String ns) {
    this.ns = ns;
  }

  public NsNameNameClass(String ns, NameClass except) {
    super(except);
    this.ns = ns;
  }

  public String getNs() {
    return ns;
  }

  public void setNs(String ns) {
    this.ns = ns;
  }

  public Object accept(NameClassVisitor visitor) {
    return visitor.visitNsName(this);
  }
}
