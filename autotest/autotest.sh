#!/bin/bash

[[ ! -f css-validator.jar ]] && \
  echo && \
  echo "Building css-validator.jar..." && \
  ant -quiet jar

javac -classpath css-validator.jar \
  ./autotest/AutoTest.java \
  ./autotest/AutoTestContentHandler.java \
  ./autotest/Result.java

[[ -z "$1" ]] && \
  echo && \
  echo "Error: autotest.sh failed to run" && \
  echo && \
  echo "Usage: autotest.sh ./testsuite/xml/FILENAME.xml" && \
  exit 1

java -cp css-validator.jar autotest.AutoTest "$1"
