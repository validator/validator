package com.thaiopensource.relaxng.util;

class ExegenDriver {
  static public void main(String[] args) {
    Driver.setUsageKey("exegen_usage");
    Driver.setParser("com.jclark.xml.sax.Driver", false);
    Driver.main(args);
  }
}
