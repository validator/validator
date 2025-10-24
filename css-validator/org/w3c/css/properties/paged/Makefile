SRCS = Css2Properties.java Css2Property.java Css2Style.java

OBJS = $(SRCS:.java=.class)

PACKAGE = org.w3c.css.properties2
JAVAC = /usr/local/java11/bin/javac
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
Css2Style.class:: Css2Style.java\
	/usr/local/java11/lib/classes.zip\
	/u/www43/0/w3c/plehegar/Work/Java/org.w3c.css.util/Warnings.class\
	/u/www43/0/w3c/plehegar/Work/Java/org.w3c.css.parser/CssPrinterStyle.class\
	/u/www43/0/w3c/plehegar/Work/Java/org.w3c.css.aural/ACssStyle.class
Css2Properties.class:: Css2Properties.java\
	/usr/local/java11/lib/classes.zip\
	/u/www43/0/w3c/plehegar/Work/Java/org.w3c.css.properties/CssProperty.class
Css2Property.class:: Css2Property.java\
	/u/www43/0/w3c/plehegar/Work/Java/org.w3c.css.aural/ACssProperty.class
