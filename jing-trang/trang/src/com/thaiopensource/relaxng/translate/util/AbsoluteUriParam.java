package com.thaiopensource.relaxng.translate.util;

import com.thaiopensource.util.Uri;

public abstract class AbsoluteUriParam extends AbstractParam {
  public void set(String value) throws InvalidParamValueException {
    if (!Uri.isValid(value))
      throw new ParamProcessor.LocalizedInvalidValueException("invalid_uri");
    if (!Uri.isAbsolute(value))
      throw new ParamProcessor.LocalizedInvalidValueException("relative_uri");
    setAbsoluteUri(value);
  }

  protected abstract void setAbsoluteUri(String value) throws InvalidParamValueException;
}
