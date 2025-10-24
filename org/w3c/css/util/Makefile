SRCS = Messages.java Warning.java\
NVPair.java Warnings.java\
InvalidParamException.java Util.java\
FakeFile.java Date.java SortedHashtable.java

OBJS = $(SRCS:.java=.class)

PACKAGE = org.w3c.css.util
JAVAC = /usr/local/java11/bin/javac
JAVADOC = javadoc
JAVADOCFLAGS = -version -d docs -protected
JAVACFLAGS =
MAKEDEPEND = jmakedepend

.SUFFIXES: .class .java
.java.class:; $(JAVAC) $(JAVACFLAGS) $<

all: $(OBJS) Messages.properties.ja Messages.properties.zh-cn

javadoc:; $(JAVADOC) $(JAVADOCFLAGS) $(PACKAGE)

clean:; -rm -f $(OBJS) *~ core

depend:; $(MAKEDEPEND) $(SRCS)

# DO NOT DELETE THIS LINE -- make depend depends on it.
InvalidParamException.class:: InvalidParamException.java\
	/u/www43/0/w3c/plehegar/Work/Java/org.w3c.css.parser.analyzer/ParseException.class
Warning.class:: Warning.java\
	/u/www43/0/w3c/plehegar/Work/Java/org.w3c.css.parser/CssSelectors.class\
	/u/www43/0/w3c/plehegar/Work/Java/org.w3c.css.properties/CssProperty.class
Util.class:: Util.java
SortedHashtable.class:: SortedHashtable.java\
	/usr/local/java11/lib/classes.zip
FakeFile.class:: FakeFile.java\
	/usr/local/java11/lib/classes.zip
Warnings.class:: Warnings.java
Date.class:: Date.java\
	/usr/local/java11/lib/classes.zip
Messages.class:: Messages.java\
	/usr/local/java11/lib/classes.zip
NVPair.class:: NVPair.java

#Messages.properties.ja: Messages.properties.ja.EUCJIS
#	native2ascii -encoding EUCJIS Messages.properties.ja.EUCJIS Messages.properties.ja 
#Messages.properties.zh-cn: Messages.properties.zh-cn.GB
#	native2ascii -encoding euc-cn Messages.properties.zh-cn.GB Messages.properties.zh-cn
