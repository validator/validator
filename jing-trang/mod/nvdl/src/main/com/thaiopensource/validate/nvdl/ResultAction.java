package com.thaiopensource.validate.nvdl;

import org.xml.sax.SAXException;
import org.xml.sax.ContentHandler;

/**
 * Reult action.
 * These actions change the sections, attach and unwrap.
 * 
 */
abstract class ResultAction extends Action {
  /**
   * Creates a result action with a given mode usage.
   * @param modeUsage The mode usage.
   */
  ResultAction(ModeUsage modeUsage) {
    super(modeUsage);
  }

  /**
   * Perform this action on a session state.
   * 
   * @param handler The content handler???
   * @param state The session state.
   * @throws SAXException
   */
  abstract void perform(ContentHandler handler, SectionState state) throws SAXException;
  
  /**
   * Get a similar action but with the current mode in the mode usage changed.
   * This is useful to get the actions from a mode that extends the mode that has this action.
   * @param mode The new current mode.
   * @return The new result action.
   */
  abstract ResultAction changeCurrentMode(Mode mode);
}
