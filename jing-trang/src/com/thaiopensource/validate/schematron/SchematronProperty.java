package com.thaiopensource.validate.schematron;

import com.thaiopensource.validate.StringPropertyId;
import com.thaiopensource.validate.FlagPropertyId;

/**
 * Properties for controlling schema reading and validation specific to Schematron.
 */
public class SchematronProperty {
  private SchematronProperty() { }

  /**
   * PropertyId that specifies the Schematron phase to use.
   * This applies during schema creation.
   */
  public static final StringPropertyId PHASE = new StringPropertyId("PHASE");

  /**
   * PropertyId thats specifies that diagnostic messages should be included.
   * This applies during validation.
   */
  public static final FlagPropertyId DIAGNOSE = new FlagPropertyId("DIAGNOSE");
}
