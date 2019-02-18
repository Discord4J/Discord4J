# Discord4J Voice
The `voice` module provides a client to manipulate audio through [Voice Connections](https://discordapp.com/developers/docs/topics/voice-connections).
## Installation
Just replace `@VERSION@` with the latest given by ![](https://img.shields.io/maven-central/v/com.discord4j/discord4j-voice.svg?style=flat-square)
### Gradle
```groovy
repositories {
  mavenCentral()
}

dependencies {
  implementation 'com.discord4j:discord4j-voice:@VERSION@'
}
```
### Maven
```xml
<dependencies>
  <dependency>
    <groupId>com.discord4j</groupId>
    <artifactId>discord4j-voice</artifactId>
    <version>@VERSION@</version>
  </dependency>
</dependencies>
```

### SBT
```scala
libraryDependencies ++= Seq(
  "com.discord4j" % "discord4j-voice" % "@VERSION@"
)
```

## Development builds
Please follow our instructions at [Using Jitpack](https://github.com/Discord4J/Discord4J/wiki/Using-Jitpack)

## Example Usage
Check out this [Example voice bot](https://github.com/Discord4J/Discord4J/blob/v3/core/src/test/java/discord4j/core/ExampleVoiceBot.java).
