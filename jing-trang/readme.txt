After checking out from the repository, here's how to build.

1. Ensure you have the a suitable version of the JDK installed: either
JDK 5.0 or 6 should work.  The JDK can be downloaded from
<http://java.sun.com/javase/downloads/>.

All other dependencies are included in the repository.

2. Set the JAVA_HOME environment variable to point to the directory
where the JDK is installed. For example, on Linux do something like

  export JAVA_HOME=/opt/jdk1.6.0_10

and, on Windows, do something like

  set JAVA_HOME=c:\Program Files\Java\jdk1.6.0_10

3. Change your working directory to be root source directory (i.e. the
directory containing this file).

4. Run the ant script included in the repository. On Linux, use the
command

  ./ant

On Windows, use

  .\ant

This runs the version of ant included in the repository.  When the ant
script completes, you should find jing.jar and trang.jar in the build
directory.

You can also tell ant to build the test target, which will build the
jars and then run some tests. On Linux,

 use the command

  ./ant test

On Windows, use

  .\ant test
