package com.thaiopensource.validate.nrl;

import com.thaiopensource.validate.Validator;
import org.xml.sax.ContentHandler;

class DelveAction extends ResultAction {
  DelveAction(ModeUsage modeUsage) {
    super(modeUsage);
  }

  void perform(ContentHandler handler, SectionState state) {
    state.addChildMode(getModeUsage(), handler);
  }

  ResultAction changeCurrentMode(Mode mode) {
    return new DelveAction(getModeUsage().changeCurrentMode(mode));
  }
}
