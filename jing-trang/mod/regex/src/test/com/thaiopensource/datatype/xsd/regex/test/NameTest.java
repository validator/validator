package com.thaiopensource.datatype.xsd.regex.test;

import com.thaiopensource.datatype.xsd.regex.Regex;
import com.thaiopensource.datatype.xsd.regex.RegexEngine;
import com.thaiopensource.datatype.xsd.regex.RegexSyntaxException;
import com.thaiopensource.util.Utf16;
import com.thaiopensource.xml.util.Naming;

public class NameTest {

  private Regex nameRegex;
  private Regex nameStartRegex;

  public static void main(String[] args) throws IllegalAccessException, InstantiationException, ClassNotFoundException, RegexSyntaxException {
      if (args.length != 1) {
      System.err.println("usage: " + NameTest.class.getName() + " engineClass");
      System.exit(2);
    }

    Class cls = NameTest.class.getClassLoader().loadClass(args[0]);
    RegexEngine engine = (RegexEngine)cls.newInstance();
    int nFail = new NameTest(engine).run();
    System.err.println(nFail + " tests failed");
    System.exit(nFail > 0 ? 1 : 0);
  }

  NameTest(RegexEngine engine) throws RegexSyntaxException {
    nameStartRegex = engine.compile("\\i");
    nameRegex = engine.compile("\\c");
  }

  int run() {
    int nFail = 0;
    for (int i = 0; i < 0x10000; i++) {
      String s = new String(new char[]{(char)i});
      if (nameRegex.matches(s) != Naming.isNmtoken(s)) {
        System.out.println("Failed for " + Integer.toHexString(i) + "; expected name == " + Naming.isNmtoken(s));
        nFail++;
      }
      if (nameStartRegex.matches(s) != Naming.isName(s)) {
        System.out.println("Failed for " + Integer.toHexString(i) + "; expected nameStart == " + Naming.isName(s));
        nFail++;
      }
    }
    for (int i = 0x10000; i < 0x110000; i++) {
      String s = new String(new char[] {Utf16.surrogate1(i), Utf16.surrogate2(i)});
      if (nameRegex.matches(s)) {
         System.out.println("Failed for " + Integer.toHexString(i) + "; expected name == false");
        nFail++;
      }
      if (nameStartRegex.matches(s)) {
        System.out.println("Failed for " + Integer.toHexString(i) + "; expected nameStart == false");
        nFail++;
      }
    }
    return nFail;
  }
}
