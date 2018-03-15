![Discord4J Logo](/images/d4j_l.png?raw=true)

# Discord4J *v3* [![Download](https://jitpack.io/v/austinv11/Discord4j.svg?style=flat-square)](https://jitpack.io/#austinv11/Discord4j)  [![Support Server Invite](https://img.shields.io/badge/Join-Discord4J-7289DA.svg?style=flat-square)](https://discord.gg/NxGAeCY) [![Documentation Status](https://readthedocs.org/projects/discord4j/badge/?version=latest)](http://discord4j.readthedocs.io/en/latest/?badge=latest&style=flat-square)

A reactive Java interface for the official [Discord](https://discordapp.com/) API, written in Java 8. Discord4J v3 is designed using [reactive principles](https://www.reactivemanifesto.org/)
using the [Reactor framework](https://projectreactor.io/) and built over [Netty](https://netty.io/) in order to create an efficient, fast and completely non-blocking API.

For a quick overview on the benefits of reactive programming/Reactor as well as a quick tutorial on Reactor click here (TODO: add link!).

## Modules
Discord4J is split up into a few "modules", these allow for extreme customizability and flexibility in how you use the library!

### [Common](https://github.com/austinv11/Secret-D4J/tree/v3/common)
The `common` module contains some simple classes used by most other modules in the library.

### [Rest](https://github.com/austinv11/Secret-D4J/tree/v3/rest)
The `rest` module allows for low level interaction with the [REST portion of the Discord API](https://discordapp.com/developers/docs/reference).

### [Gateway](https://github.com/austinv11/Secret-D4J/tree/v3/gateway)
The `gateway` module allows for low level interaction with the [gateway portion of the Discord API](https://discordapp.com/developers/docs/topics/gateway)

### [Store](https://github.com/austinv11/Secret-D4J/tree/v3/store)
The `store` module provides a set of common interfaces to create efficient caching systems for use in the [core module](https://github.com/austinv11/Secret-D4J/tree/v3/core).

### [Core](https://github.com/austinv11/Secret-D4J/tree/v3/core)
The `core` module exposes a high level API for interacting with all facets of the Discord API built on top of all the other modules, this is generally what you'd want to use.

[The API is also available in a few other languages.](https://discordapi.com/unofficial/libs.html)

## Adding Discord4J-Core as a dependency for a project
Given that `@VERSION@` = the version of Discord4J (this can either be a release version, the (short or long) commit hash or `<git branch>-SNAPSHOT`).
### With Maven
In your `pom.xml` add (without the ellipses):
```xml
<repositories>
  <repository> <!-- This repo fixes issues with transitive dependencies -->
    <id>jcenter</id>
    <url>http://jcenter.bintray.com</url>
  </repository>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>com.github.austinv11</groupId>
    <artifactId>Discord4J-core</artifactId>
    <version>@VERSION@</version>
  </dependency>
</dependencies>
```
### With Gradle
In your `build.gradle` add (without the ellipses): 
```groovy
repositories {
  jcenter() //This prevents issues with transitive dependencies
  maven {
    url  "https://jitpack.io"
  }
}

dependencies {
  compile "com.github.austinv11:Discord4J-core:@VERSION@"
}
```
### With SBT
In your `build.sbt` add (without the ellipses):
```sbt
libraryDependencies ++= Seq(
  "com.github.austinv11" % "Discord4J-core" % "@VERSION@"
)

resolvers += "jcenter" at "http://jcenter.bintray.com"
resolvers += "jitpack.io" at "https://jitpack.io"
```
### Manually with the shaded jar
TODO: Add shaded jar links for core
If you don't use Maven nor Gradle (which you really should, because it's a lot more flexible and allows you to update easily), you can always [grab the shaded jar file](http://austinv11.github.io/Discord4J/downloads.html) (which has all the D4J dependencies inside), and link it in your IntelliJ or Eclipse project.
#### IntelliJ
Module Settings > Dependencies > click the + > JARs or directories > Select your JAR file
#### Eclipse
Project Properties > Java Build Path > Add the jar file

## So, how do I use this?
### Tutorials/Resources
* TODO

### Starting with the API
TODO

### More examples
TODO

## Projects using Discord4J
TODO

## Deprecation policy
Due to the nature of the Discord API, in general any deprecations found in the API should not be expected to last past the current
version. Meaning that if a method is deprecated on version 2.1.0, do not assume the method will be available in version 2.2.0.

In some extraordinary cases, we may be forced to remove APIs without going through a deprecation cycle.

## Development
The Discord API is still in development. Functions may break at any time.  
In such an event, please contact me or submit a pull request.

## Pull requests
No one is perfect at programming and I am no exception. If you see something that can be improved, please read the [contributing guildelines](https://github.com/austinv11/Discord4J/blob/master/.github/CONTRIBUTING.md) and feel free to submit a pull request! 

## Other info
More information can be found in the official [javadocs](http://austinv11.github.io/Discord4J/docs.html). 

You can contact me on the [Official Discord4J Server (recommended)](https://discord.gg/NxGAeCY) or the [Discord API server](https://discord.gg/0SBTUU1wZTU7PCok) in the #java_discord4j channel.
