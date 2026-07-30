#!/usr/bin/env bash
# LTS Java Lab verification entry point.
# Runs standalone JDK labs, the Spring/JPA suite, and packaging checks.
#
#   ./verify.sh [java]               # full suite, including PostgreSQL containers
#   ./verify.sh [java] --no-containers
#   ./verify.sh [java] --jdk-only
#   ./verify.sh [java] --docker      # also build the production image
#
# Exit code 0 only if every selected check passes.

set -euo pipefail

JAVA="java"
MODE="full"
RUN_CONTAINERS=1
RUN_DOCKER=0
JAVA_WAS_SET=0

while (($#)); do
  case "$1" in
    --jdk-only) MODE="jdk" ;;
    --no-containers) RUN_CONTAINERS=0 ;;
    --docker) RUN_DOCKER=1 ;;
    --help|-h)
      sed -n '2,9p' "$0"
      exit 0
      ;;
    *)
      if ((JAVA_WAS_SET)); then
        echo "Unknown argument: $1" >&2
        exit 2
      fi
      JAVA="$1"
      JAVA_WAS_SET=1
      ;;
  esac
  shift
done

ROOT="$(cd "$(dirname "$0")" && pwd)"
DIR="$ROOT/labs"

echo "=============================================="
"$JAVA" -version 2>&1 | sed 's/^/  /'
echo "=============================================="

# Java 25 is the release runtime. Java 21 remains the compilation and
# compatibility floor for the Spring application.
VER=$("$JAVA" -version 2>&1 | head -1 | sed -E 's/.*"([0-9]+).*/\1/')
if [ "${VER:-0}" -lt 21 ] 2>/dev/null; then
  echo "ERROR: Java 21 or newer is required; detected Java ${VER}." >&2
  exit 2
fi
if [ "${VER:-0}" -lt 25 ] 2>/dev/null; then
  echo "NOTE: running the Java 21 compatibility lane; Java 25 is the release runtime."
  echo
fi

pass=0; fail=0; failed=()
for f in "$DIR"/*.java; do
  name=$(basename "$f" .java)
  # -ea is mandatory: standalone labs use assertions for portable contracts.
  if out=$("$JAVA" -ea "$f" 2>&1); then
    echo "PASS  $name"
    echo "$out" | grep -E '^OBSERVE ' | sed 's/^/        /' || true
    pass=$((pass+1))
  else
    echo "FAIL  $name"
    echo "$out" | sed 's/^/        /'
    fail=$((fail+1)); failed+=("$name")
  fi
done

echo "=============================================="
echo "passed: $pass   failed: $fail"
[ $fail -eq 0 ] || { printf 'failing: %s\n' "${failed[*]}"; exit 1; }
echo "Standalone JDK labs verified on this runtime."

if [[ "$MODE" == "jdk" ]]; then
  exit 0
fi

command -v mvn >/dev/null || {
  echo "Maven is required for the Spring verification suite." >&2
  exit 2
}

JAVA_HOME_VALUE=$("$JAVA" -XshowSettings:properties -version 2>&1 \
  | sed -n 's/^[[:space:]]*java.home = //p' | head -1)
[[ -n "$JAVA_HOME_VALUE" ]] || {
  echo "Could not derive JAVA_HOME from $JAVA." >&2
  exit 2
}

echo "=============================================="
if ((RUN_CONTAINERS)); then
  echo "Running Spring Boot tests with PostgreSQL Testcontainers"
  (cd "$ROOT" && JAVA_HOME="$JAVA_HOME_VALUE" mvn -q -Pcontainers clean verify)
else
  echo "Running Spring Boot tests without container-backed integration tests"
  (cd "$ROOT" && JAVA_HOME="$JAVA_HOME_VALUE" mvn -q clean test package)
fi

JAR=$(find "$ROOT/target" -maxdepth 1 -type f -name '*.jar' \
  ! -name '*.original' | head -1)
[[ -n "$JAR" ]] || {
  echo "No executable Spring Boot JAR was produced." >&2
  exit 1
}

LAYERS=$("$JAVA" -Djarmode=tools -jar "$JAR" list-layers)
for layer in dependencies spring-boot-loader snapshot-dependencies application; do
  grep -qx "$layer" <<<"$LAYERS" || {
    echo "Missing JAR layer: $layer" >&2
    exit 1
  }
done
echo "PASS  layered executable JAR (4/4 layers)"

if ((RUN_DOCKER)); then
  command -v docker >/dev/null || {
    echo "Docker is required for --docker." >&2
    exit 2
  }
  (cd "$ROOT" && docker build -t lts-java-lab:local .)
  echo "PASS  Docker image lts-java-lab:local"
fi

echo "=============================================="
echo "All selected LTS Java Lab checks are green."
