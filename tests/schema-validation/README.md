# Schema Validation Tests

This directory contains test files for validating arbitrary schemas with the vnu CLI.

## Test Files

### docbook-valid.xml

A valid DocBook 5.0 document that should pass validation when checked against the DocBook 5.1 schema.

**Test command:**
```bash
./checker.py check \
    --schema https://docbook.org/xml/5.1/rng/docbook.rng \
    --xml \
    tests/schema-validation/docbook-valid.xml
```

**Expected result:** No errors

### docbook-invalid.xml

An invalid DocBook 5.0 document containing an invalid attribute on a `para` element

**Test command:**
```bash
./checker.py check \
    --schema https://docbook.org/xml/5.1/rng/docbook.rng \
    --xml \
    tests/schema-validation/docbook-invalid.xml
```

**Expected result:** error: Attribute “unknown” not allowed on element “para” at this point

## Related Issue

These tests verify the fix for [Issue #1823](https://github.com/validator/validator/issues/1823), which enables validation of documents against arbitrary schemas using HTTPS URLs.

## Schema URL Options

The `--schema` option supports:

- **HTTP URLs:** `http://example.com/schema.rng`
- **HTTPS URLs:** `https://example.com/schema.rng`
- **File URLs:** `file:///path/to/schema.rng`

Both RELAX NG syntaxes are supported:

- **XML syntax:**		`.rng` files
- **Compact syntax:**	`.rnc` files
