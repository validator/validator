package com.thaiopensource.relaxng.mns2;

import com.thaiopensource.relaxng.ValidatorHandler;

class PassAction extends ResultAction {
  PassAction(ModeUsage modeUsage) {
    super(modeUsage);
  }

  void perform(ValidatorHandler handler, SectionState state) {
    final ModeUsage modeUsage = getModeUsage();
    if (handler != null)
      state.addActiveValidator(handler, modeUsage);
    else
      state.addAttributeValidationModeUsage(modeUsage);
    state.addChildMode(modeUsage, handler);
  }

  ResultAction changeCurrentMode(Mode mode) {
    return new PassAction(getModeUsage().changeCurrentMode(mode));
  }
}
