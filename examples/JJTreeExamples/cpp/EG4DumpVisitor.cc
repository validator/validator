/*
 * EG4DumpVisitor.cpp
 *
 *  Created on: 28 mars 2014
 *      Author: FrancisANDRE
 */

#include <iostream>
#include "JavaCC.h"
#include "EG4DumpVisitor.h"
#include "ASTMyID.h"
#include "ASTMyOtherID.h"

using namespace std;

namespace @NAMESPACE@ {

EG4DumpVisitor::EG4DumpVisitor() : indent (0) {
}

EG4DumpVisitor::~EG4DumpVisitor() {
}
JAVACC_SIMPLE_STRING EG4DumpVisitor::indentString() const {
    JAVACC_STRING_TYPE buffer;
     for (int i = 0; i < indent; i++) {
      buffer.append(" ");
    }
	return buffer;
}

void* EG4DumpVisitor::visit(const SimpleNode* node, void* data) {
//  System.out.println(indentString() + node +
//                 ": acceptor not unimplemented in subclass?");
	++indent;
	node->jjtChildrenAccept(this, data);
	--indent;
  return data;
}

void* EG4DumpVisitor::visit(const ASTStart* node, void* data) {
	cout << indentString() + node->toString() << endl;
	++indent;
	node->jjtChildrenAccept(this, data);
	--indent;
  return data;
}

void* EG4DumpVisitor::visit(const ASTAdd* node, void* data) {
	cout << indentString() + node->toString() << endl;
	++indent;
	node->jjtChildrenAccept(this, data);
	--indent;
  return data;
}

void* EG4DumpVisitor::visit(const ASTMult* node, void* data) {
	cout << indentString() + node->toString() << endl;
	++indent;
	node->jjtChildrenAccept(this, data);
	--indent;
  return data;
}

void* EG4DumpVisitor::visit(const ASTMyID* node, void* data) {
	cout << indentString() + node->toString() << endl;
	++indent;
	node->jjtChildrenAccept(this, data);
	--indent;
  return data;
}

void* EG4DumpVisitor::visit(const ASTMyOtherID* node, void* data) {
	cout << indentString() + node->toString() << endl;
	++indent;
	node->jjtChildrenAccept(this, data);
	--indent;
  return data;
}

void* EG4DumpVisitor::visit(const ASTInteger* node, void* data) {
	cout << indentString() + node->toString() << endl;
	++indent;
	node->jjtChildrenAccept(this, data);
	--indent;
  return data;
}

} /* namespace @NAMESPACE@ */
