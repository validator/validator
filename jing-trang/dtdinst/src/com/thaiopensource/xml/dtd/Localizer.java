package com.thaiopensource.xml.dtd;

import java.util.ResourceBundle;
import java.text.MessageFormat;

class Localizer {
  static final private String bundleName = "com.thaiopensource.xml.dtd.resources.Messages";

  static String message(String key) {
    return MessageFormat.format(ResourceBundle.getBundle(bundleName).getString(key),
				new Object[]{});
  }

  static String message(String key, Object arg) {
    return MessageFormat.format(ResourceBundle.getBundle(bundleName).getString(key),
			        new Object[]{arg});
  }

  static String message(String key, Object arg1, Object arg2) {
    return MessageFormat.format(ResourceBundle.getBundle(bundleName).getString(key),
			        new Object[]{arg1, arg2});
  }

  static String message(String key, Object[] args) {
    return MessageFormat.format(ResourceBundle.getBundle(bundleName).getString(key),
			        args);
  }
}
