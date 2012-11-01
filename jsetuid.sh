REPO=$HOME/.m2/repository

CLASSPATH=\
target/classes:\
$REPO/com/github/jnr/jffi/1.2.6/jffi-1.2.6.jar:\
$REPO/com/github/jnr/jffi/1.2.6/jffi-1.2.6-native.jar:\
$REPO/com/github/jnr/jnr-constants/0.8.4/jnr-constants-0.8.4.jar:\
$REPO/com/github/jnr/jnr-ffi/0.7.7/jnr-ffi-0.7.7.jar:\
$REPO/com/github/jnr/jnr-posix/2.3.1/jnr-posix-2.3.1.jar:\
$REPO/com/github/jnr/jnr-x86asm/1.0.2/jnr-x86asm-1.0.2.jar:\
$REPO/org/ow2/asm/asm/4.0/asm-4.0.jar:\
$REPO/org/ow2/asm/asm-analysis/4.0/asm-analysis-4.0.jar:\
$REPO/org/ow2/asm/asm-commons/4.0/asm-commons-4.0.jar:\
$REPO/org/ow2/asm/asm-tree/4.0/asm-tree-4.0.jar:\
$REPO/org/ow2/asm/asm-util/4.0/asm-util-4.0.jar

java -cp $CLASSPATH jsetuid.App $*
