#!/bin/sh
JING_HOME=`dirname $0`/..
PATH=$JAVA_HOME/bin:$PATH $JAVA_HOME/bin/java -Dant.home=$JING_HOME -cp $JING_HOME/lib/ant.jar:$JING_HOME/lib/crimson.jar:$JING_HOME/lib/saxon.jar:$JING_HOME/lib/optional.jar:$JAVA_HOME/lib/tools.jar:$JING_HOME/lib/regex.jar:$JING_HOME/lib/regex2.jar:$JING_HOME/lib/isorelax.jar org.apache.tools.ant.Main -buildfile $JING_HOME/build.xml $*
