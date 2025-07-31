#!/bin/sh
#
# ----------------------------------------------------------------------------
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#    https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
# ----------------------------------------------------------------------------

# ----------------------------------------------------------------------------
# Apache Maven Wrapper Startup Script
#
# Required ENV vars:
# JAVA_HOME - location of a JDK home dir
#
# Optional ENV vars
# MAVEN_SKIP_RC - flag to disable loading of mavenrc files
# ----------------------------------------------------------------------------

if [ -z "$BASH" ]; then
  case "$0" in
    */*) SCRIPT="$0" ;;
    *) SCRIPT="./$0" ;;
  esac
else
  # resolve symbolic links
  SCRIPT="${BASH_SOURCE[0]}"
fi

# resolve links - $0 may be a link
while [ -h "$SCRIPT" ] ; do
  ls=`ls -ld "$SCRIPT"`
  link=`expr "$ls" : '.*-> \(.*\)$' `
  if expr "$link" : '/.*' > /dev/null; then
    SCRIPT="$link"
  else
    SCRIPT="`dirname "$SCRIPT"`/$link"
  fi
 done

# determine maven home
. "$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.properties"

# Determine the app directory
if [ -n "$MAVEN_PROJECTBASEDIR" ] ; then
  APP_HOME="$MAVEN_PROJECTBASEDIR"
else
  APP_HOME=`cd "$MAVEN_BASEDIR"; pwd`
fi


# Attempting to set JAVA_HOME if it's empty
if [ -z "$JAVA_HOME" ]; then
  JAVA_HOME=$(dirname $(dirname $(readlink -f $(which javac))))
  if [ -z "$JAVA_HOME" ]; then
    if [ "`uname`" = "Darwin" ]; then
      JAVA_HOME=/System/Library/Frameworks/JavaVM.framework/Home
    fi
  fi
fi

MAVEN_CMD_LINE_ARGS="$MAVEN_CMD_LINE_ARGS $@"

# For Cygwin, ensure paths are in POSIX format before anything is touched
if $cygwin; then
  [ -n "$MAVEN_HOME" ] && MAVEN_HOME=`cygpath --unix "$MAVEN_HOME"`
  [ -n "$JAVA_HOME" ] && JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
fi

exec "$JAVADIR/java" $MAVEN_OPTS -classpath "$APP_HOME/.mvn/wrapper/maven-wrapper.jar" org.apache.maven.wrapper.MavenWrapperMain "$MAVEN_CMD_LINE_ARGS"
