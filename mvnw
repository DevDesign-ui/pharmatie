#!/bin/sh
# ----------------------------------------------------------------------------
# Maven Start Up Batch script
#
# This script bootstraps a Maven build using the Maven Wrapper when the
# project does not ship a local copy of Maven.
# ----------------------------------------------------------------------------

set -- \
  -Dmaven.multiModuleProjectDirectory="${MAVEN_PROJECTBASEDIR:-$(cd "$(dirname "$0")" && pwd)}" \
  "$@" \

# Determine the Java executable to use.
if [ -n "$JAVA_HOME" ]; then
  if [ -x "$JAVA_HOME/jre/sh/java" ]; then
    JAVACMD="$JAVA_HOME/jre/sh/java"
  else
    JAVACMD="$JAVA_HOME/bin/java"
  fi
  if [ ! -x "$JAVACMD" ]; then
    echo "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME" >&2
    exit 1
  fi
else
  JAVACMD="$(command -v java)"
  if [ -z "$JAVACMD" ]; then
    echo "ERROR: Unable to find any 'java' command in the PATH." >&2
    exit 1
  fi
fi

# Resolve the project base directory.
MAVEN_PROJECTBASEDIR="${MAVEN_BASEDIR:-$(cd "$(dirname "$0")" && pwd)}"
export MAVEN_PROJECTBASEDIR

# Locate the maven-wrapper jar.
WRAPPER_JAR="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"
WRAPPER_PROPS="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.properties"

if [ ! -e "$WRAPPER_JAR" ]; then
  if [ -e "$WRAPPER_PROPS" ]; then
    WRAPPER_URL=$(sed -n 's/^wrapperUrl=//p' "$WRAPPER_PROPS")
  fi
  if [ -z "$WRAPPER_URL" ]; then
    echo "ERROR: Cannot locate maven-wrapper.jar and wrapperUrl is not set." >&2
    exit 1
  fi
  echo "Downloading Maven Wrapper from $WRAPPER_URL ..."
  if command -v curl > /dev/null 2>&1; then
    curl -fsSL -o "$WRAPPER_JAR" "$WRAPPER_URL" || exit 1
  elif command -v wget > /dev/null 2>&1; then
    wget -q -O "$WRAPPER_JAR" "$WRAPPER_URL" || exit 1
  else
    echo "ERROR: Need curl or wget to download the Maven Wrapper." >&2
    exit 1
  fi
fi

exec "$JAVACMD" \
  -classpath "$WRAPPER_JAR" \
  org.apache.maven.wrapper.MavenWrapperMain "$@"
