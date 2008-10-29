package com.thaiopensource.validate.nvdl;

import org.xml.sax.ContentHandler;

class UnwrapAction extends ResultAction {
  UnwrapAction(ModeUsage modeUsage) {
    super(modeUsage);
  }

  void perform(ContentHandler handler, SectionState state) {
    state.addChildMode(getModeUsage(), handler);
  }

  ResultAction changeCurrentMode(Mode mode) {
    return new UnwrapAction(getModeUsage().changeCurrentMode(mode));
  }
}
