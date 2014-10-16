#!/bin/bash

OUTDIR=$1
ROOT=$(cd $(dirname $0); pwd)
CLASSPATH=$ROOT/lib/*:$ROOT/lib/jetty/*
MAINCLASS=org.projectbuendia.server.Server

if [ -z "$OUTDIR" ]; then
  echo "Usage: $0 <output-directory>"
  echo "    Erases <output-directory>, then compiles and runs the server there."
  exit 1
fi

# Compile the server.
$ROOT/build.sh $OUTDIR || exit 1
cd $OUTDIR

# Start the server.
java -cp .:$CLASSPATH $MAINCLASS
