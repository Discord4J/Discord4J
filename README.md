# Discord4J  [![Build Status](https://drone.io/github.com/austinv11/Discord4J/status.png)](https://drone.io/github.com/austinv11/Discord4J/latest) [ ![Download](https://api.bintray.com/packages/austinv11/maven/Discord4J/images/download.svg) ](https://bintray.com/austinv11/maven/Discord4J/_latestVersion)

Java interface for the unofficial [Discord](https://discordapp.com/) API, written in Java 8.
[The API is also available in a few other languages.](https://blog.discordapp.com/the-robot-revolution-has-unofficially-begun/)

For the latest dev builds, [download it from my ci server.](https://drone.io/github.com/austinv11/Discord4J/files)

## Adding Discord4J as a dependency for a project
Given that `@VERSION@` = the latest version of Discord4J.
### With maven
In your `pom.xml` add:
```xml
...
<repositories>
  ...
  <repository>
    <snapshots>
      <enabled>false</enabled>
    </snapshots>
    <id>bintray-austinv11-maven</id>
    <name>bintray</name>
    <url>http://dl.bintray.com/austinv11/maven</url>
  </repository>
</repositories>
...
<dependencies>
  ...
  <dependency>
    <groupId>sx.blah</groupId>
    <artifactId>Discord4J</artifactId>
    <version>@VERSION@</version>
  </dependency>
</dependencies>
...
```
### With Gradle
In your `build.gradle` add:
```groovy
...
repositories {
  ...
  maven {
    url  "http://dl.bintray.com/austinv11/maven" 
  }
}
...
dependencies {
  ...
  compile "sx.blah:Discord4J:@VERSION@"
}
...
```

## Deprecation policy
Due to the nature of the discord api, any deprecations found in the api should not be expected to last past the current version. Meaning that if a method is deprecated on version 2.1.0, do not assume the method will be available in version 2.2.0.

## Development
The Discord API is still in development. Functions may break at any time.  
In such an event, please contact me or submit a pull request.

## Pull requests
No one is perfect at programming and I am no exception. If you see something that can be improved, please feel free to submit a pull request! 

## Other info
More information can be found in the [docs](https://discordapi.readthedocs.org/en/latest/). 
You can contact me on the [Discord API server](https://discord.gg/0SBTUU1wZTU7PCok) in the #java_discord4j channel.
