package com.thaiopensource.relaxng.mns2;

import com.thaiopensource.relaxng.ValidatorHandler;
import org.xml.sax.SAXException;

abstract class ResultAction extends Action {
  ResultAction(ModeUsage modeUsage) {
    super(modeUsage);
  }

  abstract void perform(ValidatorHandler handler, SectionState state) throws SAXException;
  abstract ResultAction changeCurrentMode(Mode mode);
}
