#!/usr/bin/env bash
#
# Checks which this script does:
#
# - check that commits which modify schema/ or src/nu/validator/checker/
#   sources also include test changes.

# Get a list of all staged files (added, modified, renamed, deleted).
STAGED_FILES=$(git diff --cached --name-only)

# If there are no staged files, allow commit.
[ -z "$STAGED_FILES" ] && exit 0

# Check which sources changed.
CODE_CHANGED=$(echo "$STAGED_FILES" | grep -E '^(schema/|src/nu/validator/checker/)' || true)

# Check whether any test changes were made.
TESTS_CHANGED=$(echo "$STAGED_FILES" | grep -E '^tests/' || true)

# If code changed but tests did not: reject commit
if [ -n "$CODE_CHANGED" ] && [ -z "$TESTS_CHANGED" ]; then
    echo ""
    echo "❌ Commit rejected."
    echo ""
    echo "You are trying to commit changes to the following sources:"
    echo ""
    echo "$CODE_CHANGED"
    echo ""
    echo '…but you are not also committing changes in the “tests” directory.'
    echo ""
    echo 'When making changes to sources in the “src/nu/validator/checker”'
    echo 'or “schema” directories, you must also include test changes.'
    echo ""
    echo "But if the source changes you are trying to commit do not actually"
    echo "require associated test changes, you can bypass this check using:"
    echo ""
    echo "    git commit --no-verify"
    echo ""
    exit 1
fi

./checker.py make-messages
git diff --exit-code tests/messages.json && exit 0

if [ -z "$TESTS_CHANGED" ]; then
    echo ""
    echo "❌ Commit rejected."
    echo ""
    echo "Your changes alter the tests/messages file, but you’re not committing it."
    echo "Please include the tests/messages changes (see the diff above)."
    echo ""
    echo "Otherwise, if the omission is intentional, you can bypass this check using:"
    echo ""
    echo "    git commit --no-verify"
    echo ""
    exit 1
fi
