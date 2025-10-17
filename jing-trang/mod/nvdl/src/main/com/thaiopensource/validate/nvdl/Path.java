package com.thaiopensource.validate.nvdl;

import com.thaiopensource.xml.util.Naming;

import java.util.Vector;

/**
 * Stores a NVDL/NRL path information.
 * Parses a path string and returns a list of Path objects.
 * This stores a single path that can optionally start with a / and 
 * contains a list of local names separated by /, like
 * /path1/path2 or
 * path1/path2.
 * 
 */
class Path {
  /**
   * Flag indicating wether the path starts with / or not.
   */
  private final boolean root;

  /**
   * The list of local names that form the path.
   */
  private final Vector names;

  /**
   * Constructor, creates a Path.
   * @param root Flag specifying wether the path starts with / or not.
   * @param names The list of local names.
   */
  Path(boolean root, Vector names) {
    this.root = root;
    this.names = names;
  }

  /**
   * Determines if the path starts with / or not.
   * @return true if the path starts with /.
   */
  boolean isRoot() {
    return root;
  }

  /**
   * Get the local names list.
   * @return A vector with the local names.
   */
  Vector getNames() {
    return names;
  }

  /**
   * Get a string representation of this path.
   * It can be either /name1/name2 or name1/name2.
   */
  public String toString() {
    StringBuffer buf = new StringBuffer();
    if (root)
      buf.append('/');
    for (int i = 0, len = names.size(); i < len; i++) {
      if (i != 0)
        buf.append('/');
      buf.append((String)names.elementAt(i));
    }
    return buf.toString();
  }

  /**
   * Exception thrown in case we get errors parsing a path.
   */
  static class ParseException extends Exception {
    /**
     * The message key.
     */
    private final String messageKey;

    /**
     * Creates an exception with a given message key.
     * @param messageKey The message key.
     */
    ParseException(String messageKey) {
      super(messageKey);
      this.messageKey = messageKey;
    }

    /**
     * Get the message key.
     * @return The message key.
     */
    public String getMessageKey() {
      return messageKey;
    }
  }

  // states for parsing the path.
  
  /**
   * Initial state.
   */
  private static final int START = 0;

  /**
   * In a local name.
   */
  private static final int IN_NAME = 1;

  /**
   * After a local name.
   */
  private static final int AFTER_NAME = 2;

  /**
   * After a slash.
   */
  private static final int AFTER_SLASH = 3;

  /**
   * Gets the list of Path from the path string.
   * The path string can represent more paths separated by |.
   * 
   * @param str The path string.
   * @return A Vector with the determined Path objects.
   * @throws ParseException In case of invalid path expression.
   */
  static Vector parse(String str) throws ParseException {
    int state = START;
    int nameStartIndex = -1;
    Vector paths = new Vector();
    Vector names = new Vector();
    boolean root = false;
    for (int i = 0, len = str.length(); i < len; i++) {
      char c = str.charAt(i);
      switch (c) {
      case ' ':
      case '\r':
      case '\n':
      case '\t':
        if (state == IN_NAME) {
          names.addElement(makeName(str, nameStartIndex, i));
          state = AFTER_NAME;
        }
        break;
      case '/':
        switch (state) {
        case IN_NAME:
          names.addElement(makeName(str, nameStartIndex, i));
          break;
        case START:
          root = true;
          break;
        case AFTER_SLASH:
          throw new ParseException("unexpected_slash");
        }
        state = AFTER_SLASH;
        break;
      case '|':
        switch (state) {
        case START:
          throw new ParseException("empty_path");
        case AFTER_NAME:
          break;
        case AFTER_SLASH:
          throw new ParseException("expected_name");
        case IN_NAME:
          names.addElement(makeName(str, nameStartIndex, i));
          break;
        }
        paths.addElement(new Path(root, names));
        root = false;
        names = new Vector();
        state = START;
        break;
      default:
        switch (state) {
        case AFTER_NAME:
          throw new ParseException("expected_slash");
        case AFTER_SLASH:
        case START:
          nameStartIndex = i;
          state = IN_NAME;
          break;
        case IN_NAME:
          break;
        }
        break;
      }
    }
    switch (state) {
    case START:
      throw new ParseException("empty_path");
    case AFTER_NAME:
      break;
    case AFTER_SLASH:
      throw new ParseException("expected_name");
    case IN_NAME:
      names.addElement(makeName(str, nameStartIndex, str.length()));
      break;
    }
    paths.addElement(new Path(root, names));
    return paths;
  }

  /**
   * Extracts a name from a given string (path) from the specified
   * start position to the specified end position.
   * It also checks that the extracted name is a valid non qualified name (local name).
   * 
   * @param str The path string.
   * @param start The start position.
   * @param end The end position.
   * @return A string representing the extracted local name.
   * @throws ParseException In case of invalid local name.
   */
  private static String makeName(String str, int start, int end) throws ParseException {
    String name = str.substring(start, end);
    if (!Naming.isNcname(name))
      throw new ParseException("invalid_name");
    return name;
  }

  /**
   * Main method, for test. 
   * @param args Command line arguments, the first argument is a path.
   * @throws ParseException In case the parsing fails.
   */
  static public void main(String[] args) throws ParseException {
    Vector paths = parse(args[0]);
    for (int i = 0; i < paths.size(); i++) {
      if (i != 0)
        System.out.println("---");
      Path path = (Path)paths.elementAt(i);
      if (path.isRoot())
        System.out.println("/");
      for (int j = 0; j < path.getNames().size(); j++)
        System.out.println(path.getNames().elementAt(j));
    }
  }
}
