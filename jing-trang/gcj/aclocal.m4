m4_define([AC_LANG(Java)],
[ac_ext=java
ac_compile='$GCJ -c $GCJFLAGS conftest.$ac_ext >&AS_MESSAGE_LOG_FD'
ac_link='$GCJ --main=conftest -o conftest$ac_exeext $GCJFLAGS $LDFLAGS conftest.$ac_ext $LIBS >&AS_MESSAGE_LOG_FD'
])

AU_DEFUN([AC_LANG_JAVA], [AC_LANG(Java)])
m4_define([_AC_LANG_ABBREV(JAVA)], [java])

m4_define([AC_LANG_SOURCE(Java)],
[$1])

m4_define([AC_LANG_PROGRAM(Java)],
[$1
public class conftest {
static public void main(String[] args) {
$2
}
}])

AC_DEFUN([AC_LANG_COMPILER(Java)],
[AC_REQUIRE([AC_PROG_GCJ])])

AC_DEFUN([AC_PROG_GCJ],
[AC_LANG_PUSH(Java)dnl
AC_ARG_VAR([GCJ], [Java compiler command])
AC_ARG_VAR([GCJFLAGS], [Java compiler flags])
_AC_ARG_VAR_LDFLAGS()dnl
m4_ifval([$1],
      [AC_CHECK_TOOLS(GCJ, [$1])],
[if test -z "$GCJ"; then
  AC_CHECK_TOOL(GCJ, gcj)
fi])
test -z "$GCJ" && AC_MSG_ERROR([GCJ not found in \$PATH])
test "${GCJFLAGS+set}" = set || GCJFLAGS="-g -O2"
])

AC_DEFUN([TRY_ORG_XML_SAX],
[AC_TRY_LINK([import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.SAXException;],
[try { XMLReaderFactory.createXMLReader(); } catch (SAXException e) { }],
[$1], [$2])])

AC_DEFUN([GCJ_LIB_SAX],
[AC_MSG_CHECKING([what library contains org.xml.sax])
TRY_ORG_XML_SAX([AC_MSG_RESULT([built in])],
[LIBS=-l-org-xml-sax
TRY_ORG_XML_SAX([AC_MSG_RESULT([-l-org-xml-sax])],
[LIBS=
AC_MSG_RESULT([none found])])])])
