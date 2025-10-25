/*
 * Copyright (c) 2000 World Wide Web Consortium,
 * (Massachusetts Institute of Technology, Institut National de
 * Recherche en Informatique et en Automatique, Keio University). All
 * Rights Reserved. This program is distributed under the W3C's Software
 * Intellectual Property License. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.
 * See W3C License http://www.w3.org/Consortium/Legal/ for more details.
 *
 * $Id$
 */

/**
 * @version $Revision$
 * @author Philippe Le Hegaret
 */

package org.w3c.css.util;

public interface HTTP {

    public static final String msgs[] = {
            null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,

            "Continue",                             // 100
            "Switching Protocols",                  // 101
            null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,

            "OK",                                   // 200
            "Created",                              // 201
            "Accepted",                             // 202
            "Non-Authoritative information",        // 203
            "No Content",                           // 204
            "Reset Content",                        // 205
            "Partial Content",                      // 206
            null, null, null,
            null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,

            "Multiple Choices",                     // 300
            "Moved Permanently",                    // 301
            "Found",                                // 302
            "See Other",                            // 303
            "Not Modified",                         // 304
            "Use Proxy",                            // 305
            null,                                   // no 306 def
            "Temporary Redirect",                   // 307
            null, null,
            null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,

            "Bad Request",                          // 400
            "Unauthorized",                         // 401
            "Payment Required",                     // 402
            "Forbidden",                            // 403
            "Not Found",                            // 404
            "Method Not Allowed",                   // 405
            "Not Acceptable",                       // 406
            "Proxy Authentication Required",        // 407
            "Request Timeout",                      // 408
            "Conflict",                             // 409
            "Gone",                                 // 410
            "Length Required",                      // 411
            "Precondition Failed",                  // 412
            "Request Entity Too Large",             // 413
            "Request-URI Too Long",                 // 414
            "Unsupported Media Type",               // 415
            "Requested Range Not Satisfiable",      // 416
            "Expectation Failed",                   // 417
            null, null,
            null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,

            "Internal Server Error",                // 500
            "Not Implemented",                      // 501
            "Bad Gateway",                          // 502
            "Service Unavailable",                  // 503
            "Gateway Timeout",                      // 504
            "HTTP Version Not Supported",           // 505
            null,                                   // no 506 def
            null,                                   // no 507 def
            null,                                   // no 508 def
            null,                                   // no 509 def
            "Not Extended",                         // 510
            null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null
    };

    // HTTP status codes
    public static final int CONTINUE = 100;
    public static final int SWITCHING = 101;

    public static final int OK = 200;
    public static final int CREATED = 201;
    public static final int ACCEPTED = 202;
    public static final int NON_AUTHORITATIVE_INFORMATION = 203;
    public static final int NO_CONTENT = 204;
    public static final int RESET_CONTENT = 205;
    public static final int PARTIAL_CONTENT = 206;

    public static final int MULTIPLE_CHOICE = 300;
    public static final int MOVED_PERMANENTLY = 301;
    public static final int FOUND = 302;
    public static final int SEE_OTHER = 303;
    public static final int NOT_MODIFIED = 304;
    public static final int USE_PROXY = 305;
    public static final int TEMPORARY_REDIRECT = 307;

    public static final int BAD_REQUEST = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int PAYMENT_REQUIRED = 402;
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;
    public static final int NOT_ALLOWED = 405;
    public static final int NOT_ACCEPTABLE = 406;
    public static final int PROXY_AUTH_REQUIRED = 407;
    public static final int REQUEST_TIMEOUT = 408;
    public static final int CONFLICT = 409;
    public static final int GONE = 410;
    public static final int LENGTH_REQUIRED = 411;
    public static final int PRECONDITION_FAILED = 412;
    public static final int REQUEST_ENTITY_TOO_LARGE = 413;
    public static final int REQUEST_URI_TOO_LONG = 414;
    public static final int UNSUPPORTED_MEDIA_TYPE = 415;
    public static final int REQUESTED_RANGE_NOT_SATISFIABLE = 416;
    public static final int EXPECTATION_FAILED = 417;

    public static final int INTERNAL_SERVER_ERROR = 500;
    public static final int NOT_IMPLEMENTED = 501;
    public static final int BAD_GATEWAY = 502;
    public static final int SERVICE_UNAVAILABLE = 503;
    public static final int GATEWAY_TIMEOUT = 504;
    public static final int HTTP_VERSION_NOT_SUPPORTED = 505;
    public static final int NOT_EXTENDED = 510;


}
