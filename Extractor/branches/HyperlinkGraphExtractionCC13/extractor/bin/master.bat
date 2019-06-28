@echo off
dir /b "target\ccrdf-*.jar" > JAR
set /p JAR= < JAR
set JAR="target\%JAR%"
java -cp %JAR% org.fuberlin.wbsg.ccrdf.Master %*
