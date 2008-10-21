package com.thaiopensource.xml.tok;

/**
 * Thrown to indicate that the byte subarray being tokenized does not start
 * with a legal XML token but might be one if the subarray were extended.
 */
public class PartialTokenException extends TokenException {
}
