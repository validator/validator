package com.thaiopensource.validate.nvdl;

import org.xml.sax.SAXException;

/**
 * A no result action that rejects any element.
 */
class RejectAction extends NoResultAction {
  /**
   * Creates a reject action.
   * @param modeUsage The mode usage.
   */
  RejectAction(ModeUsage modeUsage) {
    super(modeUsage);
  }

  /**
   * Perform this action on the session state.
   * @param state The section state.
   */
  void perform(SectionState state) throws SAXException {
    final ModeUsage modeUsage = getModeUsage();
    state.reject();
    state.addChildMode(modeUsage, null);
    state.addAttributeValidationModeUsage(modeUsage);
  }

  /**
   * Get a new reject action with a mode usage with the current mode changed.
   * This is useful when we have modes extending other modes as we need to get
   * the actions from the base mode as actions on the new mode.
   */
  NoResultAction changeCurrentMode(Mode mode) {
    return new RejectAction(getModeUsage().changeCurrentMode(mode));
  }
}
