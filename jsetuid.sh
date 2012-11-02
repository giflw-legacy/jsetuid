REPO=$HOME/.m2/repository

CLASSPATH=\
target/classes:\
target/test-classes:\
$REPO/net/java/dev/jna/jna/3.5.1/jna-3.5.1.jar

java -cp $CLASSPATH com.ostendorf.jsetuid.App $*
