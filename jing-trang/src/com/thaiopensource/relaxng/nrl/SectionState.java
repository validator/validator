package com.thaiopensource.relaxng.nrl;

import com.thaiopensource.relaxng.ValidatorHandler;
import com.thaiopensource.relaxng.Schema;
import org.xml.sax.SAXException;

interface SectionState {
  /**
   *
   * @param modeUsage
   * @param handler may be null
   */
  void addChildMode(ModeUsage modeUsage, ValidatorHandler handler);
  void addNewValidator(Schema schema, ModeUsage modeUsage);
  /**
   *
   * @param handler must not be null
   */
  void addActiveValidator(ValidatorHandler handler, ModeUsage attributeModeUsage);
  void addAttributeValidationModeUsage(ModeUsage modeUsage);
  void reject() throws SAXException;
}
