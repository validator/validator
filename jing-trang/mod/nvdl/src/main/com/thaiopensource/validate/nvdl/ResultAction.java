package com.thaiopensource.validate.nvdl;

import com.thaiopensource.validate.Validator;
import com.thaiopensource.validate.nvdl.Action;
import com.thaiopensource.validate.nvdl.Mode;
import com.thaiopensource.validate.nvdl.ModeUsage;
import org.xml.sax.SAXException;
import org.xml.sax.ContentHandler;

abstract class ResultAction extends Action {
  ResultAction(ModeUsage modeUsage) {
    super(modeUsage);
  }

  abstract void perform(ContentHandler handler, SectionState state) throws SAXException;
  abstract ResultAction changeCurrentMode(Mode mode);
}
