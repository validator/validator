package com.thaiopensource.validate;

import com.thaiopensource.util.PropertyMap;

/**
 * A schema that can be used to validate an XML document. A single <code>Schema</code> object
 * is safe for concurrent access by multiple threads.
 *
 * @see com.thaiopensource.relaxng.SchemaFactory
 *
 * @author <a href="mailto:jjc@jclark.com">James Clark</a>
 */
public interface Schema {
  /**
   * Creates a new <code>ValidatorHandler</code> that validates XML documents against this
   * schema.
   *
   * @return a new <code>ValidatorHandler</code> that can be used to validate an XML document
   * against this schema; never <code>null</code>
   */
  Validator createValidator(PropertyMap properties);
}
