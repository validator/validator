package com.thaiopensource.validate.nvdl;

import org.xml.sax.SAXException;
import com.thaiopensource.validate.nvdl.Mode;
import com.thaiopensource.validate.nvdl.ModeUsage;
import com.thaiopensource.validate.nvdl.NoResultAction;

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
