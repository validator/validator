package com.thaiopensource.validate.nrl;

import com.thaiopensource.validate.ValidatorHandler;
import com.thaiopensource.validate.nrl.Mode;
import com.thaiopensource.validate.nrl.ModeUsage;

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
