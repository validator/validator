package com.thaiopensource.relaxng.translate.util;

import com.thaiopensource.xml.util.EncodingMap;

import java.io.UnsupportedEncodingException;

public abstract class EncodingParam extends AbstractParam {
  public void set(String value) throws InvalidParamValueException {
    try {
      "x".getBytes(EncodingMap.getJavaName(value));
    }
    catch (UnsupportedEncodingException e) {
      throw new ParamProcessor.LocalizedInvalidValueException("unsupported_encoding");
    }
    setEncoding(value);
  }

  protected abstract void setEncoding(String encoding) throws InvalidParamValueException;
}
