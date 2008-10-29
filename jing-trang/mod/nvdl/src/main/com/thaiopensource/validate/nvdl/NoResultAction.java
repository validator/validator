package com.thaiopensource.validate.nvdl;

import com.thaiopensource.validate.Validator;
import com.thaiopensource.validate.nvdl.Action;
import com.thaiopensource.validate.nvdl.Mode;
import com.thaiopensource.validate.nvdl.ModeUsage;
import org.xml.sax.SAXException;

abstract class NoResultAction extends Action {
  NoResultAction(ModeUsage modeUsage) {
    super(modeUsage);
  }

  abstract void perform(SectionState state) throws SAXException;
  abstract NoResultAction changeCurrentMode(Mode mode);
}
