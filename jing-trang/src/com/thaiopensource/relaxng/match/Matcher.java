package com.thaiopensource.relaxng.match;

import com.thaiopensource.xml.util.Name;
import org.relaxng.datatype.ValidationContext;

import java.util.Vector;

/**
 * Represents the state of matching an XML document against a RELAX NG pattern.
 * The XML document is considered as a linear sequence of events of different
 * kinds.  For each kind of event <var>E</var> in the sequence, a call must be made
 * to a corresponding method <code>match<var>E</var></code> on the
 * <var>Matcher</var> object.  The kinds of event are:
 *
 * <ul>
 * <li>StartDocument</li>
 * <li>StartTagOpen</li>
 * <li>AttributeName</li>
 * <li>AttributeValue</li>
 * <li>StartTagClose</li>
 * <li>Text</li>
 * <li>EndTag</li>
 * </ul>
 *
 * <p>The method calls must occur in an order corresponding to a well-formed XML
 * document.  In a well-formed document the sequence of events matches
 * the following grammar:
 *
 * <pre>
 * document ::= StartDocument <var>element</var>
 * element ::= <var>startTag</var> <var>child</var>* EndTag
 * startTag ::= StartTagOpen <var>attribute</var>* StartTagClose
 * attribute ::= AttributeName AttributeValue
 * child ::= <var>element</var> | Text
 * </pre>
 *
 * <p>Text events must be maximal.  Two consecutive Text events are not allowed.
 *
 * <p>Each method <code>match<var>E</var></code> returns false if matching
 * the event against the document resulted in an error and true otherwise.
 * If it returned false, then the error message can be obtained using
 * <code>getErrorMessage</code>.  In either case, the state of the
 * <code>Matcher</code> changes so the <code>Matcher</code> is prepared
 * to match the next event.
 *
 * <p>The <code>copy()</code> and <code>equals()</code> methods allow
 * applications to perform incremental revalidation.
 */
public interface Matcher {
  /**
   * Return a copy of the current <code>Matcher</code>.
   * Future changes to the state of the copy will not affect this and vice-versa.
   *
   * @return a <code>Matcher</code> that is a copy of this
   */
  Matcher copy();

  /**
   * Return true if obj is an equivalent <code>Matcher</code>.
   */
  boolean equals(Object obj);

  /**
   * This can only generate an error if the schema was
   * equivalent to <code>notAllowed</code>.
   *
   * @return false if there was an error, true otherwise
   */
  boolean matchStartDocument();

  boolean matchStartTagOpen(Name name);

  boolean matchAttributeName(Name name);

  /**
   * Match an attribute value.
   * The validation context must include all the namespace declarations in the start-tag
   * including those that lexically follow the attribute.
   * @param value the attribute value, normalized in accordance with XML 1.0
   * @param vc a validation context
   * @return false if there was an error, true otherwise
   */
  boolean matchAttributeValue(String value, ValidationContext vc);

  /**
   * Match the close of a start-tag (the <code>&gt;</code> character that ends the start-tag).
   * This may cause an error if there are required attributes that have not been matched.
   * @return false if there was an error, true otherwise
   */
  boolean matchStartTagClose();

  /**
   * Match a text event.
   * All text between two tags must be collected together: consecutive
   * calls to <code>matchText</code> are not allowed unless separated
   * by a call to <code>matchStartTagOpen</code> or <code>matchEndTag</code>.
   * Calls to <code>matchText</code> can sometimes be optimized into
   * calls to <code>matchUntypedText</code>.
   *
   * @param string the text to be matched
   * @param vc a validation context
   * @param nextTagIsEndTag true if the next event is an EndTag, false if it is
   * a StartTagOpen
   * @return false if there was an error, true otherwise
   */
  boolean matchText(String string, ValidationContext vc, boolean nextTagIsEndTag);

  /**
   * An optimization of <code>matchText</code>.
   * Unlike <code>matchText</code>, <code>matchUntypedText</code> does not
   * need to examine the text.
   * If <code>isTextTyped</code> returns false, then in this state
   * text that consists of whitespace may be ignored and text that contains
   * non-whitespace characters may be processed using <code>matchUntypedText</code>.
   * Furthermore it is not necessary to collect up all the text between tags;
   * consecutive calls to <code>matchUntypedText</code> are allowed.
   * <code>matchUntypedText</code> must not be used unless <code>isTextTyped</code>
   * returns false.
   *
   * @return false if there was an error, true otherwise
   */
  boolean matchUntypedText();

  /**
   * Return true if text may be typed in the current state, false otherwise.
   * If text may be typed, then a call to <code>matchText</code> must <em>not</em> be optimized
   * to <code>matchUntypedText</code>.
   * @return true if text may be typed, false otherwise
   */
  boolean isTextTyped();

  /**
   * Match an end-tag.
   * @param vc a validation context
   * @return false if there was an error, true otherwise
   */
  boolean matchEndTag(ValidationContext vc);

  /**
   * Return the current error message.
   * The current error message is changed by any <code>match<var>E</var></code> method
   * that returns false.  Initially, the current error message is null.
   * @return a string with the current error message, or null if there has not yet
   * been an error.
   */
  String getErrorMessage();

  /**
   * Return true if the document is valid so far.
   * A document is valid so far if and only if no errors have yet been
   * encountered.
   * @return true if the document is valid so far, false otherwise
   */
  boolean isValidSoFar();

  /**
   * Return a Vector of the names of elements whose start-tags are valid
   * in the current state.  This must be called only in a state in
   * which a call to <code>matchStartTagOpen</code> would be allowed.
   * The members of the Vector have type <code>com.thaiopensource.xml.util.Name</code>.
   * When an element pattern with a wildcard name-class is possible, then all
   * Names in knownNames that are contained in the wildcard name-class will be
   * included in the returned Vector.
   * @param knownNames a Vector of names to be considered for wildcards, or null
   * @return a Vector of names whose start-tags are possible
   */
  Vector possibleStartTags(Vector knownNames);

  /**
   * Return a Vector of the names of attributes that are valid
   * in the current state.  This must be called only in a state in
   * which a call to <code>matchAttributeName</code> would be allowed.
   * The members of the Vector have type <code>com.thaiopensource.xml.util.Name</code>.
   * When an attribute pattern with a wildcard name-class is possible, then all
   * Names in knownNames that are contained in the wildcard name-class will be
   * included in the returned Vector.
   * @param knownNames a Vector of names to be considered for wildcards, or null
   * @return a Vector of names of attributes that are possible
   */
  Vector possibleAttributes(Vector knownNames);
}
