package com.thaiopensource.relaxng.output.dtd;

class ContentType {
  private final ContentType parent;
  static ContentType MIXED_ELEMENT_CLASS = new ContentType();
  static ContentType NOT_ALLOWED = new ContentType();
  static ContentType SIMPLE_TYPE = new ContentType();
  static ContentType SIMPLE_TYPE_CHOICE = new ContentType(SIMPLE_TYPE);
  static ContentType VALUE = new ContentType(SIMPLE_TYPE);
  static ContentType EMPTY = new ContentType();
  static ContentType TEXT = new ContentType(MIXED_ELEMENT_CLASS);
  static ContentType MIXED_MODEL = new ContentType();
  static ContentType INTERLEAVE_MIXED_MODEL = new ContentType(MIXED_MODEL);
  static ContentType MODEL_GROUP = new ContentType();
  static ContentType ELEMENT_CLASS = new ContentType(MODEL_GROUP);
  static ContentType DIRECT_SINGLE_ELEMENT = new ContentType(ELEMENT_CLASS);
  static ContentType ZERO_OR_MORE_ELEMENT_CLASS = new ContentType(MODEL_GROUP);
  static ContentType INTERLEAVE_ZERO_OR_MORE_ELEMENT_CLASS = new ContentType(ZERO_OR_MORE_ELEMENT_CLASS);
  static ContentType ENUM = new ContentType(SIMPLE_TYPE);
  static ContentType ERROR = new ContentType();

  private ContentType() {
    this.parent = null;
  }

  private ContentType(ContentType parent) {
    this.parent = parent;
  }

  boolean isA(ContentType t) {
    if (this == t)
      return true;
    if (parent != null && parent.isA(t))
      return true;
    return false;
  }

  static ContentType zeroOrMore(ContentType t) {
    if (t.isA(ELEMENT_CLASS))
      return ZERO_OR_MORE_ELEMENT_CLASS;
    if (t.isA(MIXED_ELEMENT_CLASS))
      return MIXED_MODEL;
    return oneOrMore(t);
  }

  static ContentType oneOrMore(ContentType t) {
    if (t == ERROR)
      return ERROR;
    if (t == EMPTY)
      return EMPTY;
    if (t.isA(MODEL_GROUP))
      return MODEL_GROUP;
    return null;
  }

  static ContentType group(ContentType t1, ContentType t2) {
    if (t1.isA(MODEL_GROUP) && t2.isA(MODEL_GROUP))
      return MODEL_GROUP;
    return groupOrInterleave(t1, t2);
  }

  static ContentType mixed(ContentType t) {
    if (t.isA(EMPTY))
      return TEXT;
    if (t.isA(ZERO_OR_MORE_ELEMENT_CLASS))
      return MIXED_MODEL;
    return null;
  }

  static ContentType interleave(ContentType t1, ContentType t2) {
    if (t1.isA(ZERO_OR_MORE_ELEMENT_CLASS) && t2.isA(ZERO_OR_MORE_ELEMENT_CLASS))
      return INTERLEAVE_ZERO_OR_MORE_ELEMENT_CLASS;
    if (((t1.isA(MIXED_MODEL) || t1 == TEXT) && t2.isA(ZERO_OR_MORE_ELEMENT_CLASS))
        || t1.isA(ZERO_OR_MORE_ELEMENT_CLASS) && (t2.isA(MIXED_MODEL) || t2 == TEXT))
      return INTERLEAVE_MIXED_MODEL;
    return groupOrInterleave(t1, t2);
  }

  static private ContentType groupOrInterleave(ContentType t1, ContentType t2) {
    if (t1 == ERROR || t2 == ERROR)
      return ERROR;
    if (t1.isA(EMPTY))
      return ref(t2);
    if (t2.isA(EMPTY))
      return ref(t1);
    return null;
  }

  static ContentType optional(ContentType t) {
    if (t == ERROR)
      return ERROR;
    if (t == EMPTY)
      return EMPTY;
    if (t.isA(MODEL_GROUP))
      return MODEL_GROUP;
    if (t.isA(MIXED_ELEMENT_CLASS))
      return MIXED_ELEMENT_CLASS;
    if (t == NOT_ALLOWED)
      return MODEL_GROUP;
    return null;
  }

  static ContentType choice(ContentType t1, ContentType t2) {
    if (t1 == ERROR || t2 == ERROR)
      return ERROR;
    if (t1 == EMPTY && t2 == EMPTY)
      return EMPTY;
    if (t1 == NOT_ALLOWED) {
      if (t2 == NOT_ALLOWED)
        return NOT_ALLOWED;
      if (t2.isA(ELEMENT_CLASS))
        return ELEMENT_CLASS;
      if (t2.isA(MIXED_ELEMENT_CLASS))
        return MIXED_ELEMENT_CLASS;
      if (t2.isA(MODEL_GROUP))
        return MODEL_GROUP;
      if (t2.isA(ENUM))
        return ENUM;
      return null;
    }
    if (t2 == NOT_ALLOWED)
      return choice(t2, t1);
    if (t1.isA(ENUM) && t2.isA(ENUM))
      return ENUM;
    if (t1.isA(SIMPLE_TYPE) && t2.isA(SIMPLE_TYPE))
      return SIMPLE_TYPE_CHOICE;
    if (t1.isA(ELEMENT_CLASS) && t2.isA(ELEMENT_CLASS))
      return ELEMENT_CLASS;
    if (t1.isA(MODEL_GROUP) && t2.isA(MODEL_GROUP))
      return MODEL_GROUP;
    if ((t1.isA(MIXED_ELEMENT_CLASS) && t2.isA(ELEMENT_CLASS))
            || (t1.isA(ELEMENT_CLASS) && t2.isA(MIXED_ELEMENT_CLASS)))
      return MIXED_ELEMENT_CLASS;
    return null;
  }

  static ContentType ref(ContentType t) {
    if (t.isA(DIRECT_SINGLE_ELEMENT))
      return ELEMENT_CLASS;
    return t;
  }
}
