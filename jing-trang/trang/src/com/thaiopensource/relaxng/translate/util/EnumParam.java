package com.thaiopensource.relaxng.translate.util;

public abstract class EnumParam extends AbstractParam {
  private final String[] values;

  public EnumParam(String[] values) {
    this.values = values;
  }

  public String[] getValues() {
    return values;
  }

  public void set(String value) throws InvalidParamValueException {
    for (int i = 0; i < values.length; i++) {
      if (values[i].equals(value)) {
        setEnum(i);
        return;
      }
    }
    // XXX more helpful message
    throw new ParamProcessor.LocalizedInvalidValueException("invalid_enum");
  }

  abstract protected void setEnum(int value) throws InvalidParamValueException;
}
