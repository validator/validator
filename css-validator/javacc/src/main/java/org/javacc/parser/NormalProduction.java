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
 * Describes JavaCC productions.
 */

public class NormalProduction {

  /**
   * The line and column number of the construct that corresponds
   * most closely to this node.
   */
  private int column;

  private int line;

  /**
   * The NonTerminal nodes which refer to this production.
   */
  private List parents = new ArrayList();

  /**
   * The access modifier of this production.
   */
  private String accessMod;

  /**
   * The name of the non-terminal of this production.
   */
  private String lhs;

  /**
   * The tokens that make up the return type of this production.
   */
  private List<Token> return_type_tokens = new ArrayList<Token>();

  /**
   * The tokens that make up the parameters of this production.
   */
  private List<Token> parameter_list_tokens = new ArrayList<Token>();

  /**
   * Each entry in this list is a list of tokens that represents an
   * exception in the throws list of this production.  This list does not
   * include ParseException which is always thrown.
   */
  private List throws_list = new ArrayList();

  /**
   * The RHS of this production.  Not used for JavaCodeProduction.
   */
  private Expansion expansion;

  /**
   * This boolean flag is true if this production can expand to empty.
   */
  private boolean emptyPossible = false;

  /**
   * A list of all non-terminals that this one can expand to without
   * having to consume any tokens.  Also an index that shows how many
   * pointers exist.
   */
  private NormalProduction[] leftExpansions = new NormalProduction[10];
  int leIndex = 0;

  /**
   * The following variable is used to maintain state information for the
   * left-recursion determination algorithm:  It is initialized to 0, and
   * set to -1 if this node has been visited in a pre-order walk, and then
   * it is set to 1 if the pre-order walk of the whole graph from this
   * node has been traversed.  i.e., -1 indicates partially processed,
   * and 1 indicates fully processed.
   */
  private int walkStatus = 0;

  /**
   * The first and last tokens from the input stream that represent this
   * production.
   */
  private Token lastToken;

  private Token firstToken;

  protected String eol = System.getProperty("line.separator", "\n");
  protected StringBuffer dumpPrefix(int indent) {
    StringBuffer sb = new StringBuffer(128);
    for (int i = 0; i < indent; i++)
      sb.append("  ");
    return sb;
  }

  protected String getSimpleName() {
    String name = getClass().getName();
    return name.substring(name.lastIndexOf(".")+1); // strip the package name
  }

  public StringBuffer dump(int indent, Set alreadyDumped) {
    StringBuffer sb = dumpPrefix(indent).append(System.identityHashCode(this)).append(' ').append(getSimpleName()).append(' ').append(getLhs());
    if (!alreadyDumped.contains(this))
    {
      alreadyDumped.add(this);
      if (getExpansion() != null)
      {
        sb.append(eol).append(getExpansion().dump(indent + 1, alreadyDumped));
      }
    }

    return sb;
  }

  /**
   * @param line the line to set
   */
  public void setLine(int line) {
    this.line = line;
  }

  /**
   * @return the line
   */
  public int getLine() {
    return line;
  }

  /**
   * @param column the column to set
   */
  public void setColumn(int column) {
    this.column = column;
  }

  /**
   * @return the column
   */
  public int getColumn() {
    return column;
  }

  /**
   * @param parents the parents to set
   */
  void setParents(List parents) {
    this.parents = parents;
  }

  /**
   * @return the parents
   */
  List getParents() {
    return parents;
  }

  /**
   * @param accessMod the accessMod to set
   */
  public void setAccessMod(String accessMod) {
    this.accessMod = accessMod;
  }

  /**
   * @return the accessMod
   */
  public String getAccessMod() {
    return accessMod;
  }

  /**
   * @param lhs the lhs to set
   */
  public void setLhs(String lhs) {
    this.lhs = lhs;
  }

  /**
   * @return the lhs
   */
  public String getLhs() {
    return lhs;
  }

  /**
   * @return the return_type_tokens
   */
  public List<Token> getReturnTypeTokens() {
    return return_type_tokens;
  }

  /**
   * @return the parameter_list_tokens
   */
  public List<Token> getParameterListTokens() {
    return parameter_list_tokens;
  }

  /**
   * @param throws_list the throws_list to set
   */
  public void setThrowsList(List throws_list) {
    this.throws_list = throws_list;
  }

  /**
   * @return the throws_list
   */
  public List getThrowsList() {
    return throws_list;
  }

  /**
   * @param expansion the expansion to set
   */
  public void setExpansion(Expansion expansion) {
    this.expansion = expansion;
  }

  /**
   * @return the expansion
   */
  public Expansion getExpansion() {
    return expansion;
  }

  /**
   * @param emptyPossible the emptyPossible to set
   */
  boolean setEmptyPossible(boolean emptyPossible) {
    this.emptyPossible = emptyPossible;
    return emptyPossible;
  }

  /**
   * @return the emptyPossible
   */
  boolean isEmptyPossible() {
    return emptyPossible;
  }

  /**
   * @param leftExpansions the leftExpansions to set
   */
  void setLeftExpansions(NormalProduction[] leftExpansions) {
    this.leftExpansions = leftExpansions;
  }

  /**
   * @return the leftExpansions
   */
  NormalProduction[] getLeftExpansions() {
    return leftExpansions;
  }

  /**
   * @param walkStatus the walkStatus to set
   */
  void setWalkStatus(int walkStatus) {
    this.walkStatus = walkStatus;
  }

  /**
   * @return the walkStatus
   */
  int getWalkStatus() {
    return walkStatus;
  }

  /**
   * @param firstToken the firstToken to set
   */
  public Token setFirstToken(Token firstToken) {
    this.firstToken = firstToken;
    return firstToken;
  }

  /**
   * @return the firstToken
   */
  public Token getFirstToken() {
    return firstToken;
  }

  /**
   * @param lastToken the lastToken to set
   */
  public void setLastToken(Token lastToken) {
    this.lastToken = lastToken;
  }

  /**
   * @return the lastToken
   */
  public Token getLastToken() {
    return lastToken;
  }

}
