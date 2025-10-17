package com.thaiopensource.datatype.xsd.regex.java;

import com.thaiopensource.util.Utf16;

import java.util.regex.Pattern;

class RegexFeatures {
  static private final int EXAMPLE_NON_BMP_CHAR = 0x10000;
  static private final String EXAMPLE_SURROGATE_PAIR = new String(new char[] {
          Utf16.surrogate1(EXAMPLE_NON_BMP_CHAR),
          Utf16.surrogate2(EXAMPLE_NON_BMP_CHAR)
  });

  static final boolean SURROGATES_DIRECT =
          Pattern.compile("[^x]").matcher(EXAMPLE_SURROGATE_PAIR).matches();

  private RegexFeatures() {
  }
}
