package com.thaiopensource.validate.nrl;

import com.thaiopensource.validate.ValidatorHandler;
import com.thaiopensource.validate.nrl.Action;
import com.thaiopensource.validate.nrl.Mode;
import com.thaiopensource.validate.nrl.ModeUsage;
import org.xml.sax.SAXException;

abstract class ResultAction extends Action {
  ResultAction(ModeUsage modeUsage) {
    super(modeUsage);
  }

  abstract void perform(ValidatorHandler handler, SectionState state) throws SAXException;
  abstract ResultAction changeCurrentMode(Mode mode);
}
