package com.thaiopensource.validate.nrl;

import org.xml.sax.ContentHandler;

class IgnoreAction extends ResultAction {
  IgnoreAction(ModeUsage modeUsage) {
    super(modeUsage);
  }

  void perform(ContentHandler handler, SectionState state) {
    state.addChildMode(getModeUsage(), handler);
  }

  ResultAction changeCurrentMode(Mode mode) {
    return new IgnoreAction(getModeUsage().changeCurrentMode(mode));
  }
}
