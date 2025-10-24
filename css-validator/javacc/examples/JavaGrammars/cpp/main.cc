#include <fstream>
#include <iomanip>
#include <iostream>
#include <string>
#include <stdlib.h>

#include "gen/JavaParserConstants.h"
#include "gen/CharStream.h"
#include "gen/JavaParser.h"
#include "gen/JavaParserTokenManager.h"

using namespace java::parser;
using namespace std;

JAVACC_STRING_TYPE ReadFileFully(char *file_name) {
  JAVACC_STRING_TYPE s;
#if WIDE_CHAR
  wifstream fp_in;
#else
  ifstream fp_in;
#endif
  fp_in.open(file_name, ios::in);
  // Very inefficient.
  while (!fp_in.eof()) {
   s += fp_in.get();
  }
  return s;
}

int main(int argc, char **argv) {
  if (argc < 2) {
    cout << "Usage: wjavaparser <java inputfile>" << endl;
    exit(1);
  }
  JAVACC_STRING_TYPE s = ReadFileFully(argv[1]);
  CharStream *stream = new CharStream(s.c_str(), s.size() - 1, 1, 1);
  JavaParserTokenManager *scanner = new JavaParserTokenManager(stream);
  JavaParser parser(scanner);
  parser.setErrorHandler(new MyErrorHandler());
  parser.CompilationUnit();
  SimpleNode *root = (SimpleNode*)parser.jjtree.peekNode();
  if (root) {
    JAVACC_STRING_TYPE buffer;
#if WIDE_CHAR
    //root->dumpToBuffer(L" ", L"\n", &buffer);
    //wcout << buffer << "\n";
    root->dump(L" ");
#else
    root->dumpToBuffer(" ", "\n", &buffer);
    printf("%s\n", buffer.c_str());
#endif
  }
}
