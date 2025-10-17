package com.thaiopensource.validate.nvdl;

import org.xml.sax.ContentHandler;

/**
 * Attach place holder result action. 
 * This action replaces a section with a placeholder element.
 */
class AttachPlaceholderAction extends ResultAction {
  /**
   * Creates an attachPlaceHolder action with a given mode usage.
   * 
   * @param modeUsage
   *          The action mode usage.
   */
  AttachPlaceholderAction(ModeUsage modeUsage) {
    super(modeUsage);
  }

  /**
   * Perform this action.
   * 
   * @param handler
   *          the handler this action is performed on
   * @param state
   *          the section state.
   */
  void perform(ContentHandler handler, SectionState state) {
    state.attachPlaceholder(getModeUsage(), handler);
  }

  /**
   * Get a new attach place holder action with a mode usage with the current mode changed.
   * This is useful when we have modes extending other modes as we need to get
   * the actions from the base mode as actions on the new mode.
   */
  ResultAction changeCurrentMode(Mode mode) {
    return new AttachPlaceholderAction(getModeUsage().changeCurrentMode(mode));
  }
}
