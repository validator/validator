#include <fstream>
#include <iomanip>
#include <iostream>
#include <string>
#include <stdlib.h>
#include "JavaCC.h"
#include "Parser.h"
#include "ParseException.h"
#include "ParserTokenManager.h"

using namespace std;
using namespace EG1;

JAVACC_STRING_TYPE ReadFileFully(char *file_name) {
	return "(1 + 2) * (a + b);\n";
}

int main(int argc, char** argv) {
	cout << "Reading from standard input..." << endl;
	JAVACC_STRING_TYPE s = ReadFileFully(argv[1]);
	try {
		CharStream *stream = new CharStream(s.c_str(), s.size() - 1, 1, 1);
		ParserTokenManager *scanner = new ParserTokenManager(stream);
		Parser parser(scanner);
		SimpleNode* n = parser.Start();
		n->dump("");
		cout << "Thank you." << endl;
	} catch (const ParseException& e) {

	}
	return 0;
}

