package com.thaiopensource.validate.nvdl;

/**
 * An action that allows any element.
 */
class AllowAction extends NoResultAction {
  /**
   * Creates this no result action with a given mode usage.
   * @param modeUsage The mode usage.
   */
  AllowAction(ModeUsage modeUsage) {
    super(modeUsage);
  }

  /**
   * Perform this action on the section state.
   * @param state The section state.
   */
  void perform(SectionState state) {
    state.addChildMode(getModeUsage(), null);
    state.addAttributeValidationModeUsage(getModeUsage());
  }

  /**
   * Get a new allow action with a mode usage with the current mode changed.
   * This is useful when we have modes extending other modes as we need to get
   * the actions from the base mode as actions on the new mode.
   */
  NoResultAction changeCurrentMode(Mode mode) {
    return new AllowAction(getModeUsage().changeCurrentMode(mode));
  }
}
