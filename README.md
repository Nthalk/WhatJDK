# WhatJDK

Find out what JDK compatibility a Jar has, even one with embedded Jars!

## Usage

    ./whatjdk/whatjdk some.war
    some.war:WEB-INF/lib/xml-apis-1.4.01.jar contains classes compatible with Java1.1
    some.war contains classes compatible with Java1.6

## Requirements

Java Runtime and Maven if you plan on building the jar (probable)

## Maven Plugin

    <build>
        <plugins>
            ...
            <plugin>
                <groupId>com.iodesystems</groupId>
                <artifactId>whatjdk-maven-plugin</artifactId>
                <version>${project.version}</version>
                <configuration>
                    <allowedJdkVersions>Java1.4,Java1.5,Java1.6</allowedJdkVersions>
                </configuration>
                <executions>
                    <execution>
                        <goals>
                            <goal>verify</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
            ...
        </plugins>
    </build>
