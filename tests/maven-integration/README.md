# Maven Integration Test

This test verifies that the `nu.validator:validator` Maven artifact works correctly.

## What it tests

1. **EmbeddedValidator instantiation** - Verifies the validator can be created
2. **Shaded classes present** - Confirms that jing-trang classes are correctly shaded to `nu.validator.vendor.thaiopensource.*`
3. **No unshaded classes** - Verifies that the original `com.thaiopensource.*` classes are NOT present (to avoid conflicts)
4. **Basic validation** - Tests that HTML validation works without `NoClassDefFoundError`

## Running locally

### Test locally-built Maven artifact (recommended for development)

First, build the Maven artifacts:

```bash
python checker.py maven-artifacts
```

Then run the test:

```bash
cd tests/maven-integration
./test-maven-integration.sh local
```

This installs the locally-built artifact to your local Maven repository (`~/.m2/repository/`) and then tests it.

### Test published Maven Central version

```bash
cd tests/maven-integration

# Test the latest version from Maven Central
./test-maven-integration.sh latest

# Test a specific version
./test-maven-integration.sh 25.12.29
```

## Running in CI

The test is automatically run in GitHub Actions as part of the `maven-release` job, using the locally-built artifacts before they are published to Maven Central. This ensures the artifact works correctly before release.

## Why this test exists

Issue [#2008](https://github.com/validator/validator/issues/2008) reported that the Maven artifact was missing shaded jing-trang classes, causing `java.lang.NoClassDefFoundError: nu/validator/vendor/thaiopensource/validate/Schema` at runtime.

This test prevents regression by verifying the Maven artifact includes properly shaded classes.
