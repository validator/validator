package com.thaiopensource.datatype.xsd;

import com.thaiopensource.datatype.DatatypeContext;

class BooleanDatatype extends DatatypeBase {
  boolean lexicallyAllows(String str) {
    return str.equals("true") || str.equals("false");
  }
  Object getValue(String str, DatatypeContext dc) {
    switch (str.charAt(0)) {
    case 't':
      return Boolean.TRUE;
    }
    return Boolean.FALSE;
  }
}
