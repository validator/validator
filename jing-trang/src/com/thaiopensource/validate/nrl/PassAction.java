package com.thaiopensource.validate.nrl;

import com.thaiopensource.validate.Validator;
import com.thaiopensource.validate.nrl.Mode;
import com.thaiopensource.validate.nrl.ModeUsage;
import org.xml.sax.ContentHandler;

class PassAction extends ResultAction {
  PassAction(ModeUsage modeUsage) {
    super(modeUsage);
  }

  void perform(ContentHandler handler, SectionState state) {
    final ModeUsage modeUsage = getModeUsage();
    if (handler != null)
      state.addActiveHandler(handler, modeUsage);
    else
      state.addAttributeValidationModeUsage(modeUsage);
    state.addChildMode(modeUsage, handler);
  }

  ResultAction changeCurrentMode(Mode mode) {
    return new PassAction(getModeUsage().changeCurrentMode(mode));
  }
}
