package com.thaiopensource.xml.infer;

import org.relaxng.datatype.DatatypeLibrary;
import org.relaxng.datatype.Datatype;
import org.relaxng.datatype.DatatypeException;
import org.relaxng.datatype.DatatypeLibraryFactory;
import com.thaiopensource.util.Uri;
import com.thaiopensource.xml.util.WellKnownNamespaces;

public class DatatypeRepertoire {
  static private final int TOKEN_TYPICAL_MAX_LENGTH = 32;
  static private final int BINARY_TYPICAL_MIN_LENGTH = 128;

  static private final String[] typeNames = {
    "boolean",
    // XXX add int?
    "integer",
    "decimal",
    "double",
    "NCName",
    "NMTOKEN",
    "time",
    "date",
    "dateTime",
    "duration",
    "hexBinary",
    "base64Binary",
    "anyURI"
  };

  static public class Type {
    private final Datatype dt;
    private final String name;
    private final int index;

    private Type(Datatype dt, String name, int index) {
      this.dt = dt;
      this.name = name;
      this.index = index;
    }

    public boolean matches(String value) {
      return dt.isValid(value, null);
    }

    public boolean isTypical(String value) {
      return value.length() < TOKEN_TYPICAL_MAX_LENGTH;
    }

    public String getName() {
      return name;
    }

    public int getIndex() {
      return index;
    }
  }

  static private class BinaryType extends Type {
    private BinaryType(Datatype dt, String name, int index) {
      super(dt, name, index);
    }

    public boolean isTypical(String value) {
      return value.length() > BINARY_TYPICAL_MIN_LENGTH;
    }
  }

  static private class UriType extends Type {

    private UriType(Datatype dt, String name, int index) {
      super(dt, name, index);
    }

    public boolean isTypical(String value) {
      return Uri.isAbsolute(value) && !containsEmbeddedWhitespace(value) && !containsExcluded(value);
    }

    static private final String EXCLUDED = "<>\"{}|\\^`";

    private static boolean containsExcluded(String value) {
      for (int i = 0; i < EXCLUDED.length(); i++)
        if (value.indexOf(EXCLUDED.charAt(i)) >= 0)
          return true;
      return false;
    }

    // anyURI is derived from token so there's nothing wrong with leading and trailing whitespace
    private static boolean containsEmbeddedWhitespace(String value) {
      int state = 0;
      for (int i = 0, len = value.length(); i < len; i++)
        switch (value.charAt(i)) {
        case ' ':
        case '\t':
        case '\r':
        case '\n':
          if (state == 1)
            state = 2;
          break;
        default:
          if (state == 2)
            return true;
          if (state == 0)
            state = 1;
          break;
        }
      return false;
    }

  }

  static private class BooleanType extends Type {
    private BooleanType(Datatype dt, String name, int index) {
      super(dt, name, index);
    }

    public boolean isTypical(String value) {
      value = value.trim();
      return value.equals("true") || value.equals("false");
    }
  }

  private final Type[] types = new Type[typeNames.length];
  private int nTypes = 0;

  DatatypeRepertoire(DatatypeLibraryFactory factory) {
    DatatypeLibrary lib = factory.createDatatypeLibrary(WellKnownNamespaces.XML_SCHEMA_DATATYPES);
    if (lib == null)
      return;
    for (int i = 0; i < types.length; i++) {
      try {
        types[nTypes] = makeType(typeNames[i],
                                 lib.createDatatype(typeNames[i]),
                                 i);
        nTypes++;
      }
      catch (DatatypeException e) {
      }
    }
  }

  public int size() {
    return nTypes;
  }

  Type get(int i) {
    return types[i];
  }

  static private Type makeType(String typeName, Datatype dt, int index) {
    if (typeName.equals("anyURI"))
      return new UriType(dt, typeName, index);
    if (typeName.equals("boolean"))
      return new BooleanType(dt, typeName, index);
    if (typeName.equals("base64Binary") || typeName.equals("hexBinary"))
      return new BinaryType(dt, typeName, index);
    return new Type(dt, typeName, index);
  }

  public static String getUri() {
    return WellKnownNamespaces.XML_SCHEMA_DATATYPES;
  }
}
