package com.thaiopensource.relaxng.nrl;

import com.thaiopensource.relaxng.ValidatorHandler;
import com.thaiopensource.relaxng.Schema;
import com.thaiopensource.util.Localizer;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public interface SectionState {
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
