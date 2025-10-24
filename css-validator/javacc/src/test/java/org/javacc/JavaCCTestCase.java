/**
 *
 */
package org.javacc;

import junit.framework.TestCase;

/**
 * An ancestor class to enable transition to a different directory structure.
 *
 * @author timp
 * @since 2 Nov 2007
 *
 */
public abstract class JavaCCTestCase extends TestCase {

  /**
   *
   */
  public JavaCCTestCase() {
    super();
  }

  /**
   * @param name the test name
   */
  public JavaCCTestCase(String name) {
    super(name);
  }

  /**
   * @return the documentation output directory name String relative to the root
   */
  public String getJJDocOutputDirectory() {
    return "www/doc/";
    //return "src/site/resources/";
  }

  /**
   * Where the input jj files are located
   * @return the directory name String relative to the root
   */
  public String getJJInputDirectory() {
    return "src/main/javacc/";
    //return "src/org/javacc/parser/";
    //return "src/main/javacc/org/javacc/parser/";
  }
}
