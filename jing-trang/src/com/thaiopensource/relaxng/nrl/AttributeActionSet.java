package com.thaiopensource.relaxng.nrl;

import com.thaiopensource.relaxng.Schema;

class AttributeActionSet {
  private boolean pass;
  private boolean reject;
  private Schema[] schemas = new Schema[0];

  boolean getPass() {
    return pass;
  }

  void setPass(boolean pass) {
    this.pass = pass;
  }

  boolean getReject() {
    return reject;
  }

  void setReject(boolean reject) {
    this.reject = reject;
  }

  Schema[] getSchemas() {
    return schemas;
  }

  void addSchema(Schema schema) {
    Schema[] s = new Schema[schemas.length + 1];
    System.arraycopy(schemas, 0, s, 0, schemas.length);
    s[schemas.length] = schema;
    schemas = s;
  }
}
