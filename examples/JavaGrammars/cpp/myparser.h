#include "gen/ErrorHandler.h"
#include "gen/Token.h"
#include "gen/JavaParser.h"

namespace java { namespace parser {

class MyParser {
};

class MyErrorHandler: public ErrorHandler {
      virtual void handleUnexpectedToken(int expectedKind, JAVACC_STRING_TYPE expectedToken, Token *actual, JavaParser *parser) { }
      virtual void handleParseError(Token *last, Token *unexpected, JAVACC_SIMPLE_STRING production, JavaParser *parser) { }

};

} }
