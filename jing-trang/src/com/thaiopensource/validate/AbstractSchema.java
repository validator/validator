package com.thaiopensource.validate;

/**
 * A skeleton implementation of <code>Schema</code>.
 *
 * @author <a href="mailto:jjc@jclark.com">James Clark</a>
 */
public abstract class AbstractSchema implements Schema {
  /**
   * Calls <code>createValidator(null)</code>.
   */
  public ValidatorHandler createValidator() {
    return createValidator(null);
  }
}
