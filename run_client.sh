#!/bin/bash

# Ensure we are in the project directory
dirname=$(dirname "$0")
cd "$dirname"

echo "Check/Compiling client..."
if [ ! -d "bin" ]; then
    mkdir bin
fi

find src -name "*.java" > sources_tmp.txt
javac -d bin -sourcepath src -cp "lib/mysql-connector-j-8.0.33.jar" @sources_tmp.txt
rm sources_tmp.txt

echo "Starting Whiteboard Client..."
java -cp "bin:lib/mysql-connector-j-8.0.33.jar" com.whiteboard.client.WhiteboardApp
