package com.thaiopensource.relaxng.mns2;

import com.thaiopensource.relaxng.ValidatorHandler;

class DelveAction extends ResultAction {
  DelveAction(ModeUsage modeUsage) {
    super(modeUsage);
  }

  void perform(ValidatorHandler handler, SectionState state) {
    state.addChildMode(getModeUsage(), handler);
  }

  ResultAction changeCurrentMode(Mode mode) {
    return new DelveAction(getModeUsage().changeCurrentMode(mode));
  }
}
