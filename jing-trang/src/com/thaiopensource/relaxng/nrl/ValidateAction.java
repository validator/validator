package com.thaiopensource.relaxng.nrl;

import com.thaiopensource.relaxng.Schema;
import org.xml.sax.SAXException;

class ValidateAction extends NoResultAction {
  private final Schema schema;

  ValidateAction(ModeUsage modeUsage, Schema schema) {
    super(modeUsage);
    this.schema = schema;
  }

  void perform(SectionState state) throws SAXException {
    state.addNewValidator(schema, getModeUsage());
  }

  NoResultAction changeCurrentMode(Mode mode) {
    return new ValidateAction(getModeUsage().changeCurrentMode(mode), schema);
  }

  public boolean equals(Object obj) {
    return super.equals(obj) && schema.equals(((ValidateAction)obj).schema);
  }

  public int hashCode() {
    return super.hashCode() ^ schema.hashCode();
  }
}
