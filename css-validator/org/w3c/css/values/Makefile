SRCS = CssFrequency.java CssPercentage.java\
CssAngle.java CssIdent.java CssString.java\
CssColor.java CssLength.java CssTime.java\
CssColorConstants.java CssNumber.java CssURL.java\
CssExpression.java CssOperator.java CssValue.java \
CssFunction.java

OBJS = $(SRCS:.java=.class)

PACKAGE = org.w3c.css.values
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
CssURL.class:: CssURL.java\
	/u/crash/0/w3c/plehegar/Work/Java/java/net/URL.class\
	/u/crash/0/w3c/plehegar/Work/Java/java/net/MalformedURLException.class
CssColor.class:: CssColor.java\
	/u/crash/0/w3c/plehegar/Work/Java/java/util/Hashtable.class
CssNumber.class:: CssNumber.java
CssColorConstants.class:: CssColorConstants.java
CssValue.class:: CssValue.java
CssTime.class:: CssTime.java
CssString.class:: CssString.java
CssFunction.class:: CssFunction.java
CssOperator.class:: CssOperator.java
CssIdent.class:: CssIdent.java
CssPercentage.class:: CssPercentage.java
CssFrequency.class:: CssFrequency.java
CssAngle.class:: CssAngle.java
CssExpression.class:: CssExpression.java\
	/u/crash/0/w3c/plehegar/Work/Java/java/util/Vector.class
CssLength.class:: CssLength.java
