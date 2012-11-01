REPO=$HOME/.m2/repository

CLASSPATH=\
target/classes:\
$REPO/net/java/dev/jna/jna/3.5.1/jna-3.5.1.jar:\
$REPO/org/jruby/ext/posix/jna-posix/1.0.3/jna-posix-1.0.3.jar

java -cp $CLASSPATH jsetuid.App $*
