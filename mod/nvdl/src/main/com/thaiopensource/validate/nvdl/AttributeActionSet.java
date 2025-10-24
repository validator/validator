package com.thaiopensource.validate.nvdl;

import com.thaiopensource.validate.Schema;

/**
 * Action set for attributes.
 * Consistes of two flags (attach, reject) and a list of schemas.
 */
class AttributeActionSet {
  /**
   * Attach flag.
   */
  private boolean attach;
  
  /**
   * Reject flag.
   */
  private boolean reject;
  
  /**
   * Cancel nested actions flag.
   */
  private boolean cancelNestedActions;
  
  /**
   * An array of schemas.
   */
  private Schema[] schemas = new Schema[0];

  /**
   * Getter for the attach flag.
   * @return attach.
   */
  boolean getAttach() {
    return attach;
  }

  /**
   * Setter for the attach flag.
   * 
   * @param attach The new attach value.
   */
  void setAttach(boolean attach) {
    this.attach = attach;
  }

  /**
   * Getter for the reject flag.
   * @return reject.
   */
  boolean getReject() {
    return reject;
  }

  /**
   * Setter for the reject flag.
   * @param reject The new reject flag value.
   */
  void setReject(boolean reject) {
    this.reject = reject;
  }

  /**
   * Getter for the cancel nested actions flag. 
   */
  boolean getCancelNestedActions() {
    return cancelNestedActions;
  }
  
  /**
   * Set the cancel nested actions flag.
   * @param cancelNestedActions The new value.
   */
  void setCancelNestedActions(boolean cancelNestedActions) {
    this.cancelNestedActions = cancelNestedActions;
  }
  
  /**
   * Get the schemas array.
   * @return The array of Schema objects.
   */
  Schema[] getSchemas() {
    return schemas;
  }

  /**
   * Add a new Schema.
   * @param schema The schema to be added.
   */
  void addSchema(Schema schema) {
    Schema[] s = new Schema[schemas.length + 1];
    System.arraycopy(schemas, 0, s, 0, schemas.length);
    s[schemas.length] = schema;
    schemas = s;
  }
}
