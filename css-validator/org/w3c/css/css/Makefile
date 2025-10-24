SRCS = CssCascadingOrder.java StyleSheetCom.java \
CssParser.java StyleSheetGenerator.java \
StyleSheetParser.java StyleSheet.java\
HTMLStyleSheetParser.java StyleSheetXMLParser.java \


OBJS = $(SRCS:.java=.class)

PACKAGE = org.w3c.css.css
JAVAC = jc -val
JAVADOC = javadoc
JAVADOCFLAGS = -version -d docs -protected
JAVACFLAGS =
MAKEDEPEND = jmakedepend

.SUFFIXES: .class .java
.java.class:; $(JAVAC) $(JAVACFLAGS) $<

all: $(OBJS) html.properties.ja html.properties.zh-cn

javadoc:; $(JAVADOC) $(JAVADOCFLAGS) $(PACKAGE)

clean:; -rm -f $(OBJS) *~ core

depend:; $(MAKEDEPEND) $(SRCS)

# DO NOT DELETE THIS LINE -- make depend depends on it.
StyleSheetParser.class:: StyleSheetParser.java
StyleSheetCom.class:: StyleSheetCom.java
StyleSheetGenerator.class:: StyleSheetGenerator.java
CssParser.class:: CssParser.java
CssCascadingOrder.class:: CssCascadingOrder.java
HTMLStyleSheetParser.class:: HTMLStyleSheetParser.java
StyleSheet.class:: StyleSheet.java

html.properties.ja: html.properties.ja.EUCJIS
	native2ascii -encoding EUCJIS html.properties.ja.EUCJIS html.properties.ja
html.properties.zh-cn: html.properties.zh-cn.GB
	native2ascii -encoding euc-cn html.properties.zh-cn.GB html.properties.zh-cn
