#!/usr/bin/env bash
mkdir temp
rm temp/*.*
cp target/javacc-6.2.1.jar temp
cp target/javacc-6.2.1-sources.jar temp
cp target/javacc-6.2.1-javadoc.jar temp
cp deployment_pom/javacc-6.2.1.pom temp

cd temp

gpg -ab javacc-6.2.1.jar
gpg -ab javacc-6.2.1-sources.jar
gpg -ab javacc-6.2.1-javadoc.jar
gpg -ab javacc-6.2.1.pom

jar -cvf bundle.jar javacc-6.2.1.pom javacc-6.2.1.pom.asc javacc-6.2.1.jar javacc-6.2.1.jar.asc javacc-6.2.1-javadoc.jar javacc-6.2.1-javadoc.jar.asc javacc-6.2.1-sources.jar javacc-6.2.1-sources.jar.asc

cd ..
