package com.thaiopensource.relaxng.nrl;

import org.xml.sax.SAXException;

class RejectAction extends NoResultAction {
  RejectAction(ModeUsage modeUsage) {
    super(modeUsage);
  }

  void perform(SectionState state) throws SAXException {
    final ModeUsage modeUsage = getModeUsage();
    state.reject();
    state.addChildMode(modeUsage, null);
    state.addAttributeValidationModeUsage(modeUsage);
  }

  NoResultAction changeCurrentMode(Mode mode) {
    return new RejectAction(getModeUsage().changeCurrentMode(mode));
  }
}
