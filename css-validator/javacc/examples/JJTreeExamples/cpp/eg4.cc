#include <fstream>
#include <iomanip>
#include <iostream>
#include <string>
#include <stdlib.h>
#include "Parser.h"
#include "EG4DumpVisitor.h"
#include "ParseException.h"
#include "MyErrorHandler.h"
#include "ParserTokenManager.h"

using namespace std;
using namespace EG4;

JAVACC_STRING_TYPE ReadFileFully(char *file_name) {
//	JAVACC_STRING_TYPE s;
//#if WIDE_CHAR
//	wifstream in;
//#else
//	ifstream in;
//#endif
//	in.open(file_name, ios::in);
//	// Very inefficient.
//	while (!in.eof()) {
//		s += in.get();
//	}
//	return s;
	return "(1 + 2) * (a + b);\n";
}

int main(int argc, char** argv) {
	cout << "Reading from standard input..." << endl;
	JAVACC_STRING_TYPE s = ReadFileFully(argv[1]);
	try {
		CharStream *stream = new CharStream(s.c_str(), s.size() - 1, 1, 1);
		ParserTokenManager *scanner = new ParserTokenManager(stream);
		Parser parser(scanner);
		parser.setErrorHandler(new MyErrorHandler());
		ASTStart* n = parser.Start();
		EG4DumpVisitor eg4dv;
		eg4dv.visit(n, NULL);
		cout << "Thank you." << endl;
	} catch (const ParseException& e) {

	}
	return 0;
}

