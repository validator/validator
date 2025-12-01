#!/usr/bin/env bash

# the file containing the commit message is passed as the first argument
commit_file="$1"
commit_message=$(cat "$commit_file")

error() {
  echo -e "\033[0;31m$1:\033[0m"
  echo ""
  echo "------------------------------------------------------------------"
  echo "$commit_message"
  echo "------------------------------------------------------------------"
  echo ""
  echo "If your commit message for some reason intentially does not follow"
  echo "the requirements, you can bypass these checks using:"
  echo ""
  echo "    git commit --no-verify"
  exit 1
}

# fail if the commit message contains windows style line breaks (carriage returns)
if grep -q -U $'\x0D' "$commit_file"; then
  error "Commit message contains CRLF (Windows) line breaks. Only unix-style LF linebreaks are allowed."
fi

line_number=0
while read -r line; do
  # break on git cut line, used by git commit --verbose
  if [[ "$line" == "# ------------------------ >8 ------------------------" ]]; then
    break
  fi

  # ignore comment lines
  [[ "$line" =~ ^#.* ]] && continue
  # ignore overlong 'fixup!' commit descriptions
  [[ "$line" =~ ^fixup!\ .* ]] && continue

  ((line_number += 1))
  line_length=${#line}

  if [[ $line_number -eq 2 ]] && [[ $line_length -ne 0 ]]; then
    error "You must put an empty line between commit title and body."
  fi

  merge_commit_pattern="^Merge branch"
  if [[ $line_number -eq 1 ]] && (echo "$line" | grep -E -q "$merge_commit_pattern"); then
    error "Commit is a git merge commit; use the rebase command instead."
  fi

  if [[ $line_number -eq 1 ]] && [[ "$line" =~ \.$ ]]; then
    error "Commit-message title ends in a period"
  fi

  url_pattern="([a-z]+:\/\/)?(([a-zA-Z0-9_]|-)+\.)+[a-z]{2,}(:\d+)?([a-zA-Z_0-9@:%\+.~\?&\/=]|-)+"
  if [[ $line_length -gt 80 ]] && (echo "$line" | grep -E -v -q "$url_pattern"); then
    error "Some commit-message lines are longer than 80 characters. Please wrap them.)"
  fi

  if [[ "$line" == *"commit message was AI-generated"* ]]; then
    error 'Commit-message body contains “commit message was AI-generated” text.'
  fi

done <"$commit_file"
exit 0
