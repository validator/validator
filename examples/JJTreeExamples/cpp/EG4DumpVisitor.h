/*
 * EG4DumpVisitor.h
 *
 *  Created on: 28 mars 2014
 *      Author: FrancisANDRE
 */

#ifndef EG4DUMPVISITOR_H_
#define EG4DUMPVISITOR_H_
#include "ParserVisitor.h"
#include "JavaCC.h"

namespace @NAMESPACE@ {
class ASTMyID;
class ASTMyOtherID;

class EG4DumpVisitor: public ParserVisitor {
public:
	EG4DumpVisitor();
	virtual ~EG4DumpVisitor();

	/**
	 *  This is an example of how the Visitor pattern might be used to
	 *  implement the dumping code that comes with SimpleNode.  It's a bit
	 *  long-winded, but it does illustrate a couple of the main points.
	 *  <ol>
	 *  <li> the visitor can maintain state between the nodes that it visits
	 *  (for example the current indentation level).
	 *  </li>
	 *
	 *  <li>if you don't implement a jjtAccept() method for a subclass of
	 *  SimpleNode, then SimpleNode's acceptor will get called.
	 *  </li>
	 *  <li> the utility method childrenAccept() can be useful when
	 *  implementing preorder or postorder tree walks.
	 *  </li>
	 *  </ol>
	 *
	 */

private:
	int indent;
	JAVACC_SIMPLE_STRING indentString() const;
public:
	void* visit(const SimpleNode *node, void * data);
	void* visit(const ASTStart *node, void * data);
	void* visit(const ASTAdd *node, void * data);
	void* visit(const ASTMult *node, void * data);
	void* visit(const ASTMyID *node, void * data);
	void* visit(const ASTMyOtherID *node, void * data);
	void* visit(const ASTInteger *node, void * data);
};

} /* namespace @NAMESPACE@ */

#endif /* EG4DUMPVISITOR_H_ */
