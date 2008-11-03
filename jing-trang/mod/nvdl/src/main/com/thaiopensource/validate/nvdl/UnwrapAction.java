package com.thaiopensource.validate.nvdl;

import org.xml.sax.ContentHandler;

/**
 * Unwrap result action.
 * This action an element but allows its content.
 */
class UnwrapAction extends ResultAction {
  /**
   * Creates an unwrap action with a given mode usage.
   * @param modeUsage The action mode usage.
   */
  UnwrapAction(ModeUsage modeUsage) {
    super(modeUsage);
  }

  /**
   * Perform this action.
   * 
   * @param handler ???
   * @param state the section state.
   */
  void perform(ContentHandler handler, SectionState state) {
    state.addChildMode(getModeUsage(), handler);
  }

  /**
   * Get a new unwrap action with a mode usage with the current mode changed.
   * This is useful when we have modes extending other modes as we need to get
   * the actions from the base mode as actions on the new mode.
   */
  ResultAction changeCurrentMode(Mode mode) {
    return new UnwrapAction(getModeUsage().changeCurrentMode(mode));
  }
}
