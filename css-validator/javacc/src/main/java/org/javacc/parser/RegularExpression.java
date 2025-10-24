/* Copyright (c) 2006, Sun Microsystems, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Sun Microsystems, Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived from
 *       this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.javacc.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Describes regular expressions.
 */

public abstract class RegularExpression extends Expansion {

  /**
   * The label of the regular expression (if any).  If no label is
   * present, this is set to "".
   */
  public String label = "";

  /**
   * The ordinal value assigned to the regular expression.  It is
   * used for internal processing and passing information between
   * the parser and the lexical analyzer.
   */
  int ordinal;

  /**
   * The LHS to which the token value of the regular expression
   * is assigned.  In case there is no LHS, then the list
   * remains empty.
   */
  public List<Token> lhsTokens = new ArrayList<Token>();

  /**
   * We now allow qualified access to token members. Store it here.
   */
  public Token rhsToken;

  /**
   * This flag is set if the regular expression has a label prefixed
   * with the # symbol - this indicates that the purpose of the regular
   * expression is solely for defining other regular expressions.
   */
  public boolean private_rexp = false;

  /**
   * If this is a top-level regular expression (nested directly
   * within a TokenProduction), then this field point to that
   * TokenProduction object.
   */
  public TokenProduction tpContext = null;

  public abstract Nfa GenerateNfa(boolean ignoreCase);

  public boolean CanMatchAnyChar()
  {
     return false;
  }

  /**
   * The following variable is used to maintain state information for the
   * loop determination algorithm:  It is initialized to 0, and
   * set to -1 if this node has been visited in a pre-order walk, and then
   * it is set to 1 if the pre-order walk of the whole graph from this
   * node has been traversed.  i.e., -1 indicates partially processed,
   * and 1 indicates fully processed.
   */
  int walkStatus = 0;

  public StringBuffer dump(int indent, Set<? super Expansion> alreadyDumped) {
    StringBuffer sb = super.dump(indent, alreadyDumped);
    alreadyDumped.add(this);
    sb.append(' ').append(label);
    return sb;
  }
}
