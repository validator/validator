package com.thaiopensource.relaxng.mns2;

import com.thaiopensource.relaxng.ValidatorHandler;

class AllowAction extends NoResultAction {
  AllowAction(ModeUsage modeUsage) {
    super(modeUsage);
  }

  void perform(SectionState state) {
    state.addChildMode(getModeUsage(), null);
    state.addAttributeValidationModeUsage(getModeUsage());
  }
}
