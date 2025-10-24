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
package org.javacc.jjtree;

import java.util.Enumeration;
import java.util.Hashtable;

public class NodeScope
{
  ASTProduction production;
  ASTNodeDescriptor node_descriptor;

  String closedVar;
  String exceptionVar;
  String nodeVar;
  int scopeNumber;

  NodeScope(ASTProduction p, ASTNodeDescriptor n)
  {
    production = p;

    if (n == null) {
      String nm = production.name;
      if (JJTreeOptions.getNodeDefaultVoid()) {
        nm = "void";
      }
      node_descriptor = ASTNodeDescriptor.indefinite(nm);
    } else {
      node_descriptor = n;
    }

    scopeNumber = production.getNodeScopeNumber(this);
    nodeVar = constructVariable("n");
    closedVar = constructVariable("c");
    exceptionVar = constructVariable("e");
  }


  boolean isVoid()
  {
    return node_descriptor.isVoid();
  }


  ASTNodeDescriptor getNodeDescriptor()
  {
    return node_descriptor;
  }


  String getNodeDescriptorText()
  {
    return node_descriptor.getDescriptor();
  }


  String getNodeVariable()
  {
    return nodeVar;
  }


  private String constructVariable(String id)
  {
    String s = "000" + scopeNumber;
    return "jjt" + id + s.substring(s.length() - 3, s.length());
  }


  boolean usesCloseNodeVar()
  {
    return true;
  }

  static NodeScope getEnclosingNodeScope(Node node)
  {
    if (node instanceof ASTBNFDeclaration) {
      return ((ASTBNFDeclaration)node).node_scope;
    }
    for (Node n = node.jjtGetParent(); n != null; n = n.jjtGetParent()) {
      if (n instanceof ASTBNFDeclaration) {
        return ((ASTBNFDeclaration)n).node_scope;
      } else if (n instanceof ASTBNFNodeScope) {
        return ((ASTBNFNodeScope)n).node_scope;
      } else if (n instanceof ASTExpansionNodeScope) {
        return ((ASTExpansionNodeScope)n).node_scope;
      }
    }
    return null;
  }

}

/*end*/
