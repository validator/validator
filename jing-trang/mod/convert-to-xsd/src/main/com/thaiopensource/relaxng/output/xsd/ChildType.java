package com.thaiopensource.relaxng.output.xsd;

class ChildType {
  static private final int ALLOW_EMPTY = 01;
  static private final int ALLOW_ELEMENT = 02;
  static private final int ALLOW_ATTRIBUTE = 04;
  static private final int ALLOW_DATA = 010;
  static private final int ALLOW_TEXT = 020;
  private final int flags;

  static final ChildType NOT_ALLOWED = new ChildType(0);
  static final ChildType EMPTY = new ChildType(ALLOW_EMPTY);
  static final ChildType ELEMENT = new ChildType(ALLOW_ELEMENT);
  static final ChildType ATTRIBUTE = new ChildType(ALLOW_ATTRIBUTE);
  static final ChildType DATA = new ChildType(ALLOW_DATA);
  static final ChildType TEXT = new ChildType(ALLOW_TEXT);

  private ChildType(int flags) {
    this.flags = flags;
  }

  public boolean equals(Object obj) {
    return obj instanceof ChildType && ((ChildType)obj).flags == this.flags;
  }

  public int hashCode() {
    return flags;
  }

  static ChildType choice(ChildType ct1, ChildType ct2) {
    return new ChildType(ct1.flags | ct2.flags);
  }

  static ChildType group(ChildType ct1, ChildType ct2) {
    if (ct1.flags == 0 || ct2.flags == 0)
      return NOT_ALLOWED;
    return new ChildType(((ct1.flags | ct2.flags) & ~ALLOW_EMPTY)
                         | (ct1.flags & ct2.flags & ALLOW_EMPTY));
  }

  boolean contains(ChildType ct) {
    return (flags & ct.flags) == ct.flags;
  }
}
