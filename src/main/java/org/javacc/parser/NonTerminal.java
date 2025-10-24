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
 * Describes non terminals.
 */

public class NonTerminal extends Expansion {

  /**
   * The LHS to which the return value of the non-terminal
   * is assigned.  In case there is no LHS, then the vector
   * remains empty.
   */
  private List<Token> lhsTokens = new ArrayList<Token>();

  /**
   * The name of the non-terminal.
   */
  private String name;

  /**
   * The list of all tokens in the argument list.
   */
  private List<Token> argument_tokens = new ArrayList<Token>();

  private List<Token> parametrized_type__tokens = new ArrayList<Token>();
  /**
   * The production this non-terminal corresponds to.
   */
  private NormalProduction prod;

  public StringBuffer dump(int indent, Set alreadyDumped) {
    StringBuffer value = super.dump(indent, alreadyDumped).append(' ').append(name);
    return value;
  }

/**
 * @param lhsTokens the lhsTokens to set
 */
public void setLhsTokens(List<Token> lhsTokens) {
	this.lhsTokens = lhsTokens;
}

/**
 * @return the lhsTokens
 */
public List<Token> getLhsTokens() {
	return lhsTokens;
}

/**
 * @param name the name to set
 */
public void setName(String name) {
	this.name = name;
}

/**
 * @return the name
 */
public String getName() {
	return name;
}

/**
 * @param argument_tokens the argument_tokens to set
 */
public void setParametrizedTypeTokens(List<Token> argument_tokens) {
	this.argument_tokens = argument_tokens;
}

/**
 * @return the argument_tokens
 */
public List<Token> getParametrizedTypeTokens() {
	return parametrized_type__tokens;
}

/**
 * @param argument_tokens the argument_tokens to set
 */
public void setArgumentTokens(List<Token> parametrized_type__tokens) {
	this.parametrized_type__tokens = parametrized_type__tokens;
}

/**
 * @return the argument_tokens
 */
public List<Token> getArgumentTokens() {
	return argument_tokens;
}

/**
 * @param prod the prod to set
 */
public NormalProduction setProd(NormalProduction prod) {
	return this.prod = prod;
}

/**
 * @return the prod
 */
public NormalProduction getProd() {
	return prod;
}
}
