package com.thaiopensource.relaxng.translate.util;

import com.thaiopensource.xml.util.Naming;

public abstract class NmtokenParam extends AbstractParam {
  public void set(String value) throws InvalidParamValueException {
    if (!Naming.isNmtoken(value))
      throw new ParamProcessor.LocalizedInvalidValueException("invalid_nmtoken");
    setNmtoken(value);
  }

  protected abstract void setNmtoken(String value) throws InvalidParamValueException;
}
