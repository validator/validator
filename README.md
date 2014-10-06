---------------------------------------------------------------------------
How to download, build, and run the validator
---------------------------------------------------------------------------
1. First, set the JAVA_HOME environment variable properly.

   export JAVA_HOME=/usr/lib/jvm/java-6-openjdk on Ubuntu or

   export JAVA_HTML=/Library/Java/Home on Mac OS X.

2. Create a validator workspace.

   mkdir checker

   cd checker

   git clone https://github.com/validator/build.git build

   The above steps creates a "checker" directory in which the build script
   will create other subdirectories, and downloads the build.py script
   itself into a "build" subdirectory in your checker directory.

3. Run the build script.

   python build/build.py all

   python build/build.py all

   Yes, the last line is there twice intentionally. Running the script
   twice tends to fix a ClassCastException on the first run.

The above will download, build and run the system at http://localhost:8888/.
For other options, please run python build/build.py --help instead.

Please note that the dependencies are big. The script will spend time
downloading stuff. The script requires Python, Git, and JDK 5 or later
(JDK 6 and Hardy’s OpenJDK work). (Tested on Mac OS X and Ubuntu with the
openjdk-6-jdk package.)

Note: The script wants to see a Sun-compatible jar executable. Debian
fastjar will not work.

---------------------------------------------------------------------------
Deployment
---------------------------------------------------------------------------
The above example starts a standalone HTTP server with debug messages
printed to the console. To use AJP13 instead, use --ajp=on. A log4j
configuration for deployment can be given using the --log4j= option. There
is a sample file in validator/log4j-deployment-sample.properties. The
directory extras/ is searched for additional jars for the classpath. For
example, if you configure log4j to send email, you should put the Java Mail
API and JavaBeans Activation Framework jars in extras/.
