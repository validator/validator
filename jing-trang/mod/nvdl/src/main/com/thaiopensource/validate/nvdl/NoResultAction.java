package com.thaiopensource.validate.nvdl;

import org.xml.sax.SAXException;
/**
 * No result action. These actions are the validations, either validate, allow or reject.
 */
abstract class NoResultAction extends Action {
  /**
   * Creates the action with the specified mode usage.
   * @param modeUsage The mode usage.
   */
  NoResultAction(ModeUsage modeUsage) {
    super(modeUsage);
  }
  /**
   * Perform this action on the SectionState.
   * @param state The section state.
   * @throws SAXException In case of errors.
   */
  abstract void perform(SectionState state) throws SAXException;
  
  /**
   * Get a no result action with the current mode changed.
   * @param mode the new mode.
   * @return A new no result action.
   */
  abstract NoResultAction changeCurrentMode(Mode mode);
}
