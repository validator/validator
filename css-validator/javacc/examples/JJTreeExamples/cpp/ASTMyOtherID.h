/*
 * ASTMyOtherID.h
 *
 *  Created on: 27 mars 2014
 *      Author: FrancisANDRE
 */

#ifndef ASTMYOTHERID_H_
#define ASTMYOTHERID_H_
#include "ParserTree.h"
#include "SimpleNode.h"

namespace @NAMESPACE@ {
class ParserVisitor;

class ASTMyOtherID:  public SimpleNode {
private:
	JAVACC_SIMPLE_STRING name;

public:
	ASTMyOtherID(int id);
	ASTMyOtherID(Parser *p, int id);
	virtual ~ASTMyOtherID();

	void setName(JAVACC_STRING_TYPE image);
	JAVACC_STRING_TYPE toString() const;

	double x;

	/** Accept the visitor. **/
	virtual void* jjtAccept(ParserVisitor *visitor, void * data) const;

};
}
#endif /* ASTMYOTHERID_H_ */
