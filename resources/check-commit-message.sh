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
  error "Please use Unix line breaks (LF), not Windows line breaks (CRLF)"
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
    error "Please put an empty line between commit-message title and body"
  fi

  merge_commit_pattern="^Merge branch"
  if [[ $line_number -eq 1 ]] && (echo "$line" | grep -E -q "$merge_commit_pattern"); then
    error 'Please do not make “git merge” commits; use “git rebase“ instead'
  fi

  category_pattern='^(Revert "|\S+: )'
  if [[ $line_number -eq 1 ]] && (echo "$line" | grep -E -v -q "$category_pattern"); then
    error 'Please add a prefix to the commit-message title; e.g., add “fix:”, “feat:”, “chore:”, “test:”'
  fi

  title_case_pattern="^\S.*?: [A-Z0-9]"
  if [[ $line_number -eq 1 ]] && (echo "$line" | grep -E -v -q "$title_case_pattern"); then
    error "Please capitalize the first word of the commit-message title after the prefix"
  fi

  if [[ $line_number -eq 1 ]] && [[ "$line" =~ \.$ ]]; then
    error "Please do not put a period at the end of the commit-message title"
  fi

  url_pattern="([a-z]+:\/\/)?(([a-zA-Z0-9_]|-)+\.)+[a-z]{2,}(:\d+)?([a-zA-Z_0-9@:%\+.~\?&\/=]|-)+"
  if [[ $line_length -gt 80 ]] && (echo "$line" | grep -E -v -q "$url_pattern"); then
    error "Please wrap all commit-message lines to 80 characters or less"
  fi

  if [[ "$line" == *"commit message was AI-generated"* ]]; then
    error 'Please remove any “commit message was AI-generated” text'
  fi

done <"$commit_file"
exit 0
