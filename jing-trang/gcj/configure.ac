AC_INIT(@PROG@, @PROG_VERSION@)
AC_PROG_INSTALL
AC_ARG_VAR([GCJ], [Java compiler command])
AC_ARG_VAR([GCJFLAGS], [Java compiler flags])
AC_ARG_VAR([LDFLAGS], [linker flags])
if test -z "$GCJ"; then
  AC_CHECK_TOOL(GCJ, gcj)
fi
test -z "$GCJ" && AC_MSG_ERROR([GCJ not found in \$PATH])
test "${GCJFLAGS+set}" = set || GCJFLAGS="-g -O2"
AC_CONFIG_FILES([Makefile test/Makefile])
AC_OUTPUT
