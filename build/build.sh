#!/bin/sh
if [ "$1" != "" ]; then
  args=$*;
else
  args="run";
fi
if [ -z "$PYTHON" ]; then
  PYTHON=python
fi
$PYTHON ./checker.py $args
