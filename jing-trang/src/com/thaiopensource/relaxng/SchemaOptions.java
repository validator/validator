package com.thaiopensource.relaxng;

public class SchemaOptions {
  private static final int CHECK_ID_IDREF_FLAG = 01;
  private static final int FEASIBLE_FLAG = 02;
  private static final int MAX_FLAG = FEASIBLE_FLAG;
  private static final SchemaOptions[] cache = new SchemaOptions[MAX_FLAG << 1];

  public static final SchemaOptions NONE = getInstance(0);
  public static final SchemaOptions CHECK_ID_IDREF = getInstance(CHECK_ID_IDREF_FLAG);
  public static final SchemaOptions FEASIBLE = getInstance(FEASIBLE_FLAG);

  private final int flags;

  private SchemaOptions(int flags) {
    this.flags = flags;
  }

  public boolean contains(SchemaOptions other) {
    return (this.flags & other.flags) == other.flags;
  }

  public SchemaOptions add(SchemaOptions options) {
    return getInstance(this.flags | options.flags);
  }

  public SchemaOptions remove(SchemaOptions options) {
    return getInstance(this.flags & ~options.flags);
  }

  private static synchronized SchemaOptions getInstance(int flags) {
    if (cache[flags] != null)
      return cache[flags];
    return cache[flags] = new SchemaOptions(flags);
  }
}
