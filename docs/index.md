# Welcome

<img align="right" src="https://raw.githubusercontent.com/Discord4J/discord4j-web/master/public/logo.svg?sanitize=true" width=27%>

Welcome to the Discord4J wiki! Discord4J is a [reactive](https://www.reactivemanifesto.org/) Java wrapper for the official [Discord Bot API](https://discordapp.com/developers/docs/intro). This wiki will cover all the basics on reactive programming, how to utilize the Discord4J library effectively, and common examples in reactive and blocking contexts. Feel free to explore the various topics this wiki covers using the links in the sidebar.

## Download / Installation
The recommended way to get Discord4J is to use a build automation tool like [Maven](https://maven.apache.org/) or [Gradle](https://gradle.org/). To set up Maven or Gradle, refer to the documentation for your specific IDE:

* [IntelliJ / Maven](https://www.jetbrains.com/help/idea/maven-support.html)
* [IntelliJ / Gradle](https://www.jetbrains.com/help/idea/getting-started-with-gradle.html)
* [Eclipse / Maven](http://www.vogella.com/tutorials/EclipseMaven/article.html)
* [Eclipse / Gradle](http://www.vogella.com/tutorials/EclipseGradle/article.html)

#### Versions

| Discord4J                                                   | Support          | Gateway/API | Intents                           |
|-------------------------------------------------------------|------------------|-------------|-----------------------------------|
| [v3.2.x](https://github.com/Discord4J/Discord4J/tree/master)| In development   | v8          | Mandatory, non-privileged default |
| [v3.1.x](https://github.com/Discord4J/Discord4J/tree/3.1.x) | Current          | v6          | Optional, no intent default       |
| [v3.0.x](https://github.com/Discord4J/Discord4J/tree/3.0.x) | Maintenance only | v6          | No intents support                |

Replace VERSION below with one of these:

* Latest version from v3.2.x: [![Maven Central](https://img.shields.io/maven-central/v/com.discord4j/discord4j-core/3.2.svg?style=flat-square)](https://search.maven.org/artifact/com.discord4j/discord4j-core)
* Latest version from v3.1.x: [![Maven Central](https://img.shields.io/maven-central/v/com.discord4j/discord4j-core/3.1.svg?style=flat-square)](https://search.maven.org/artifact/com.discord4j/discord4j-core)
* Latest version from v3.0.x: [![Maven Central](https://img.shields.io/maven-central/v/com.discord4j/discord4j-core/3.0.svg?style=flat-square)](https://search.maven.org/artifact/com.discord4j/discord4j-core)

### Maven
```xml
<dependencies>
  <dependency>
    <groupId>com.discord4j</groupId>
    <artifactId>discord4j-core</artifactId>
    <version>VERSION</version>
  </dependency>
</dependencies>
```
### Gradle
```groovy
repositories {
  mavenCentral()
}

dependencies {
  implementation 'com.discord4j:discord4j-core:VERSION'
}
```
### SBT
```scala
libraryDependencies ++= Seq(
  "com.discord4j" % "discord4j-core" % "VERSION"
)
```
If you prefer using experimental, "bleeding-edge", unstable builds, refer to [Using Jitpack](Using-Jitpack.md).

## Logging
While optional, we do recommend installing and configuring a logging implementation to aid in debugging and provide useful information for day-to-day operations; plus, it's [good practice](https://softwareengineering.stackexchange.com/questions/37294/logging-why-and-what). Discord4J uses Reactor's [logging implementation](https://projectreactor.io/docs/core/release/reference/#_logging_a_sequence), which is compatible with any [SLF4J](https://www.slf4j.org/) implementation. We recommend using [Logback](https://logback.qos.ch/) for maximum flexibility and customization; check our dedicated [Logging](Logging.md) guide for details.
### Maven
```xml
<dependencies>
  <dependency>
    <groupId>ch.qos.logback</groupId>
    <artifactId>logback-classic</artifactId>
    <version>${logback_version}</version>
  </dependency>
</dependencies>
```
### Gradle
```groovy
dependencies {
  implementation 'ch.qos.logback:logback-classic:$logback_version'
}
```
### SBT
```scala
libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % s"$logbackVersion"
)
```
