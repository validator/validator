package com.thaiopensource.validate.picl;

abstract class Pattern {
  abstract boolean matches(Path path, int rootDepth);
}
