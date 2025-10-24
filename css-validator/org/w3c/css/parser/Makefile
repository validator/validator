SRCS = CssFouffa.java Errors.java\
CssError.java CssValidatorListener.java Frame.java\
CssErrorDeclaration.java \
CssErrorToken.java CssSelectors.java\
CssStyle.java CssPrinterStyle.java CssParseException.java

OBJS = $(SRCS:.java=.class)

PACKAGE = org.w3c.css.parser
JAVAC = jc -val
JAVADOC = javadoc
JAVADOCFLAGS = -version -d docs -protected
JAVACFLAGS = 
MAKEDEPEND = jmakedepend

.SUFFIXES: .class .java
.java.class:; $(JAVAC) $(JAVACFLAGS) $<

all: $(OBJS)

javadoc:; $(JAVADOC) $(JAVADOCFLAGS) $(PACKAGE)

clean:; -rm -f $(OBJS) *~ core

depend:; $(MAKEDEPEND) $(SRCS)
# DO NOT DELETE THIS LINE -- make depend depends on it.
CssErrorToken.class:: CssErrorToken.java
Frame.class:: Frame.java
CssError.class:: CssError.java
CssValidatorListener.class:: CssValidatorListener.java
Errors.class:: Errors.java
CssFouffa.class:: CssFouffa.java
CssStyle.class:: CssStyle.java
CssParseException.class:: CssParseException.java
CssPrinterStyle.class:: CssPrinterStyle.java
