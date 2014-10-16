#!/bin/bash

OUTDIR=$1
ROOT=$(cd $(dirname $0); pwd)
CLASSPATH=$ROOT/lib/*:$ROOT/lib/jetty/*
SRCDIR=$ROOT/src

if [ -z "$OUTDIR" ]; then
  echo "Usage: $0 <output-directory>"
  echo "    Erases <output-directory>, then compiles the server there."
  exit 1
fi

# Compile all the source files into class files.
rm -rf $OUTDIR
mkdir -p $OUTDIR
echo "Compiling..."
javac -cp $CLASSPATH $(find $SRCDIR -name '*.java') -d $OUTDIR || exit 1
echo "...class files written to $OUTDIR successfully."

# The server needs config.prop, install/ddl.sql, and errors/ to exist.
cp -pr $ROOT/config.prop $ROOT/install $OUTDIR
mkdir $OUTDIR/errors
