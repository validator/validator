package com.thaiopensource.validate;

import org.xml.sax.ErrorHandler;
import com.thaiopensource.validate.ValidatorHandler;

/**
 * A schema that can be used to validate an XML document. A single <code>Schema</code> object
 * is safe for concurrent access by multiple threads.
 *
 * @see com.thaiopensource.validate.AbstractSchema
 * @see com.thaiopensource.relaxng.SchemaFactory
 *
 * @author <a href="mailto:jjc@jclark.com">James Clark</a>
 */
public interface Schema {
  /**
   * Creates a new <code>ValidatorHandler</code> that validates XML documents against this
   * schema. The <code>ValidatorHandler</code> will report errors using the specified
   * <code>ErrorHandler</code>. The <code>ErrorHandler</code> of the created
   * <code>ValidatorHandler</code> may be changed at any time using <code>setErrorHandler</code>.
   *
   * @param eh the <code>ErrorHandler</code> to be used by the <code>ValidatorHandler</code>
   * for reporting errors; <code>null</code> if errors should not be reported
   * @return a new <code>ValidatorHandler</code> that can be used to validate an XML document
   * against this schema; never <code>null</code>
   */
  ValidatorHandler createValidator(ErrorHandler eh);

  /**
   * Creates a new <code>ValidatorHandler</code> that validates XML documents against this
   * schema.  The <code>ValidatorHandler</code> will have a <code>null</code> <code>ErrorHandler</code>,
   * which may be changed at any time using <code>setErrorHandler</code>.
   *
   * @return a new <code>ValidatorHandler</code> that can be used to validate an XML document
   * against this schema; never <code>null</code>
   */
  ValidatorHandler createValidator();
}
