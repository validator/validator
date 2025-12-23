#!/usr/bin/env python3
"""
Shade jing.jar using Maven Shade Plugin
"""
import os
import sys
import subprocess
import shutil
import tempfile


def main():
    # Determine Maven command based on platform
    mvn_cmd = "mvn.cmd" if sys.platform == "win32" else "mvn"

    # Paths
    jing_jar = "jing-trang/build/jing.jar"
    isorelax_jar = "jing-trang/lib/isorelax.jar"
    output_jar = "build/jing-shaded.jar"

    if not os.path.exists(jing_jar):
        print(f"Error: {jing_jar} not found. Build jing-trang first.")
        sys.exit(1)

    if not os.path.exists(isorelax_jar):
        print(f"Error: {isorelax_jar} not found.")
        sys.exit(1)

    with tempfile.TemporaryDirectory() as tmpdir:
        print(f"Working in temporary directory: {tmpdir}")

        print("Installing jing.jar to local Maven repository...")
        install_cmd = [
            mvn_cmd, "install:install-file",
            f"-Dfile={os.path.abspath(jing_jar)}",
            "-DgroupId=com.thaiopensource",
            "-DartifactId=jing",
            "-Dversion=20091111",
            "-Dpackaging=jar",
            "-DgeneratePom=true"
        ]

        result = subprocess.run(
            install_cmd,
            capture_output=True,
            text=True
        )

        if result.returncode != 0:
            print("Maven install failed!")
            print(f"STDERR:\n{result.stderr}")
            sys.exit(1)

        print("Successfully installed jing.jar to local Maven repo")

        print("Installing isorelax.jar to local Maven repository...")
        install_cmd = [
            mvn_cmd, "install:install-file",
            f"-Dfile={os.path.abspath(isorelax_jar)}",
            "-DgroupId=isorelax",
            "-DartifactId=isorelax",
            "-Dversion=20030108",
            "-Dpackaging=jar",
            "-DgeneratePom=true"
        ]

        result = subprocess.run(
            install_cmd,
            capture_output=True,
            text=True
        )

        if result.returncode != 0:
            print("Maven install failed!")
            print(f"STDERR:\n{result.stderr}")
            sys.exit(1)

        print("Successfully installed isorelax.jar to local Maven repo")

        pom_content = """<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <groupId>nu.validator</groupId>
  <artifactId>jing-shaded</artifactId>
  <version>1.0.0</version>
  <packaging>jar</packaging>

  <name>Shaded Jing</name>
  <description>Shaded version of jing-trang to avoid Maven conflicts</description>

  <dependencies>
    <dependency>
      <groupId>com.thaiopensource</groupId>
      <artifactId>jing</artifactId>
      <version>20091111</version>
    </dependency>
    <dependency>
      <groupId>isorelax</groupId>
      <artifactId>isorelax</artifactId>
      <version>20030108</version>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-shade-plugin</artifactId>
        <version>3.5.1</version>
        <executions>
          <execution>
            <phase>package</phase>
            <goals>
              <goal>shade</goal>
            </goals>
            <configuration>
              <createDependencyReducedPom>false</createDependencyReducedPom>
              <relocations>
                <relocation>
                  <pattern>com.thaiopensource</pattern>
                  <shadedPattern>nu.validator.vendor.thaiopensource</shadedPattern>
                </relocation>
                <relocation>
                  <pattern>org.relaxng</pattern>
                  <shadedPattern>nu.validator.vendor.relaxng</shadedPattern>
                </relocation>
                <relocation>
                  <pattern>org.iso_relax</pattern>
                  <shadedPattern>nu.validator.vendor.iso_relax</shadedPattern>
                </relocation>
                <relocation>
                  <pattern>jp.gr.xml.relax</pattern>
                  <shadedPattern>nu.validator.vendor.jp.gr.xml.relax</shadedPattern>
                </relocation>
              </relocations>
              <transformers>
                <transformer implementation="org.apache.maven.plugins.shade.resource.ServicesResourceTransformer"/>
              </transformers>
            </configuration>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
</project>
"""  # nopep8

        work_pom = os.path.join(tmpdir, "pom.xml")
        with open(work_pom, 'w') as f:
            f.write(pom_content)

        print("Running Maven package with shade plugin...")
        cmd = [mvn_cmd, "clean", "package"]

        result = subprocess.run(
            cmd,
            cwd=tmpdir,
            capture_output=True,
            text=True
        )

        if result.returncode != 0:
            print("Maven command failed!")
            print(f"STDOUT:\n{result.stdout}")
            print(f"STDERR:\n{result.stderr}")
            sys.exit(1)

        target_dir = os.path.join(tmpdir, "target")
        shaded_jar = os.path.join(target_dir, "jing-shaded-1.0.0.jar")

        if not os.path.exists(shaded_jar):
            print(f"Error: Shaded jar not found at {shaded_jar}")
            print(f"Maven output:\n{result.stdout}")
            sys.exit(1)

        os.makedirs(os.path.dirname(output_jar), exist_ok=True)
        shutil.copy(shaded_jar, output_jar)

        print(f"Successfully created {output_jar}")
        print(f"Shaded jar size: {os.path.getsize(output_jar)} bytes")


if __name__ == '__main__':
    main()
