#!/bin/sh

JAVA_HOME=/usr/local/java
export JAVA_HOME

PATH=$JAVA_HOME/bin/:$PATH
export PATH

cd /usr/local/validator.nu/checker

if [ "$1" != "" ]
then
	args=$@
else
	args="--no-self-update run"
fi
python build/build.py \
  --hgRoot=http://dvcs.w3.org/hg/ \
  --w3cbranding=1 \
  --heap=512 \
  --connection-timeout=3 \
  --socket-timeout=3 \
  --page-template=http://www.w3.org/2009/12/vnu/w3cPageEmitter.xml \
  --form-template=http://www.w3.org/2009/12/vnu/w3cFormEmitter.xml \
  --html5link=http://dev.w3.org/html5/spec-author-view/ \
  --about=http://www.w3.org/2009/12/vnu/ \
  --name="Ready to validate" \
  $args
