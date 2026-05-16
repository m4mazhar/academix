#!/bin/zsh
set -euo pipefail

PROJECT_ROOT="/Users/mazharul.islam/Documents/my-works/academix"
MAIN_SRC="$PROJECT_ROOT/src/main/java"
TEST_SRC="$PROJECT_ROOT/src/test/java"

echo "Migrating source packages to com.edumerge..."

# Update package declarations and imports in all Java files.
for root in "$MAIN_SRC" "$TEST_SRC"; do
  if [[ -d "$root" ]]; then
    find "$root" -name '*.java' -print0 | while IFS= read -r -d '' file; do
      perl -0pi -e 's/package com\.academix\.demo(\.[A-Za-z0-9_]+)?;/package com.edumerge$1;/g; s/import com\.academix\.demo\./import com.edumerge./g' "$file"
    done
  fi
done

# Optional directory cleanup for the main source tree.
if [[ -d "$MAIN_SRC/com/academix" ]]; then
  mkdir -p "$MAIN_SRC/com/edumerge"
  find "$MAIN_SRC/com/academix" -type f -name '*.java' -print0 | while IFS= read -r -d '' file; do
    rel="${file#"$MAIN_SRC/com/academix/demo/"}"
    dest="$MAIN_SRC/com/edumerge/$rel"
    mkdir -p "$(dirname "$dest")"
    cp "$file" "$dest"
  done
  echo "Copied source files into com/edumerge/."
  echo "After verifying builds, you can remove com/academix/ manually."
fi

echo "Done."
