package com.thaiopensource.relaxng.translate.util;

public abstract class IntegerParam extends AbstractParam {
  private final int minValue;
  private final int maxValue;

  public IntegerParam(int minValue, int maxValue) {
    this.minValue = minValue;
    this.maxValue = maxValue;
  }

  public IntegerParam() {
    this(Integer.MIN_VALUE, Integer.MAX_VALUE);
  }

  public void set(String value) throws InvalidParamValueException {
    try {
      int n = Integer.parseInt(value);
      if (n < minValue || n > maxValue)
        throw new ParamProcessor.LocalizedInvalidValueException("out_of_range_integer");
     setInteger(n);
    }
    catch (NumberFormatException e) {
      throw new ParamProcessor.LocalizedInvalidValueException("not_an_integer");
    }
  }

  protected abstract void setInteger(int value) throws InvalidParamValueException;
}
