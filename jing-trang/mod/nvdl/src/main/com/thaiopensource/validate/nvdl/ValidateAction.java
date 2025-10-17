package com.thaiopensource.validate.nvdl;

import com.thaiopensource.validate.Schema;
import org.xml.sax.SAXException;

/**
 * Validate no result action.
 *
 */
class ValidateAction extends NoResultAction {
  /**
   * The schema to validate with.
   */
  private final Schema schema;

  /**
   * Creates a validate action.
   * @param modeUsage The mode usage.
   * @param schema The schema.
   */
  ValidateAction(ModeUsage modeUsage, Schema schema) {
    super(modeUsage);
    this.schema = schema;
  }

  /**
   * Perform this action on the section state.
   * @param state the section state.
   */
  void perform(SectionState state) throws SAXException {
    state.addValidator(schema, getModeUsage());
  }

  /**
   * Get a new validate action with a mode usage with the current mode changed.
   * This is useful when we have modes extending other modes as we need to get
   * the actions from the base mode as actions on the new mode.
   */
  NoResultAction changeCurrentMode(Mode mode) {
    return new ValidateAction(getModeUsage().changeCurrentMode(mode), schema);
  }

  /**
   * Checks if this action is equal with a given action.
   */
  public boolean equals(Object obj) {
    return super.equals(obj) && schema.equals(((ValidateAction)obj).schema);
  }

  /**
   * Computes a hashCode.
   */
  public int hashCode() {
    return super.hashCode() ^ schema.hashCode();
  }
}
