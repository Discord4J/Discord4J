# Discord4J Voice
The `voice` module provides a client to manipulate audio through [Voice Connections](https://discord.com/developers/docs/topics/voice-connections).

## Installation
1. Replace `@D4J_VERSION@` with the latest given by ![](https://img.shields.io/maven-central/v/com.discord4j/discord4j-voice.svg?style=flat-square)
2. Implement one of the natives implementation of libdave for your OS and architecture.
3. Replace `@LIBDAVE_VERSION@` with the latest given by ![](https://img.shields.io/maven-central/v/moe.kyokobot.libdave/natives-linux-x86-64.svg?style=flat-square)

### Gradle
```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation 'com.discord4j:discord4j-voice:@D4J_VERSION@'
    
    // NOTE: Keep only the natives that match your OS and architecture.
    // You don't need to include all of them.
    
    // Linux (glibc 2.28 / EL8)
    implementation("moe.kyokobot.libdave:natives-linux-x86-64:@LIBDAVE_VERSION@")
    
    // Linux (glibc 2.35)
    implementation("moe.kyokobot.libdave:natives-linux-x86:@LIBDAVE_VERSION@")
    implementation("moe.kyokobot.libdave:natives-linux-aarch64:@LIBDAVE_VERSION@")
    implementation("moe.kyokobot.libdave:natives-linux-arm:@LIBDAVE_VERSION@")
    
    // Linux (musl)
    implementation("moe.kyokobot.libdave:natives-linux-musl-x86-64:@LIBDAVE_VERSION@")
    implementation("moe.kyokobot.libdave:natives-linux-musl-x86:@LIBDAVE_VERSION@")
    implementation("moe.kyokobot.libdave:natives-linux-musl-aarch64:@LIBDAVE_VERSION@")
    implementation("moe.kyokobot.libdave:natives-linux-musl-arm:@LIBDAVE_VERSION@")
    
    // Windows
    implementation("moe.kyokobot.libdave:natives-win-x86-64:@LIBDAVE_VERSION@")
    implementation("moe.kyokobot.libdave:natives-win-x86:@LIBDAVE_VERSION@")
    implementation("moe.kyokobot.libdave:natives-win-aarch64:@LIBDAVE_VERSION@")
    
    // macOS
    implementation("moe.kyokobot.libdave:natives-darwin:@LIBDAVE_VERSION@")
}
```
### Maven
```xml

<dependencies>
    <dependency>
        <groupId>com.discord4j</groupId>
        <artifactId>discord4j-voice</artifactId>
        <version>@D4J_VERSION@</version>
    </dependency>

    <!-- NOTE: Keep only the natives that match your OS and architecture. -->
    <!-- You don't need to include all of them. -->

    <!-- Linux (glibc 2.28 / EL8) -->
    <dependency>
        <groupId>moe.kyokobot.libdave</groupId>
        <artifactId>natives-linux-x86-64</artifactId>
        <version>@LIBDAVE_VERSION@</version>
    </dependency>

    <!-- Linux (glibc 2.35) -->
    <dependency>
        <groupId>moe.kyokobot.libdave</groupId>
        <artifactId>natives-linux-x86</artifactId>
        <version>@LIBDAVE_VERSION@</version>
    </dependency>
    <dependency>
        <groupId>moe.kyokobot.libdave</groupId>
        <artifactId>natives-linux-aarch64</artifactId>
        <version>@LIBDAVE_VERSION@</version>
    </dependency>
    <dependency>
        <groupId>moe.kyokobot.libdave</groupId>
        <artifactId>natives-linux-arm</artifactId>
        <version>@LIBDAVE_VERSION@</version>
    </dependency>

    <!-- Linux (musl) -->
    <dependency>
        <groupId>moe.kyokobot.libdave</groupId>
        <artifactId>natives-linux-musl-x86-64</artifactId>
        <version>@LIBDAVE_VERSION@</version>
    </dependency>
    <dependency>
        <groupId>moe.kyokobot.libdave</groupId>
        <artifactId>natives-linux-musl-x86</artifactId>
        <version>@LIBDAVE_VERSION@</version>
    </dependency>
    <dependency>
        <groupId>moe.kyokobot.libdave</groupId>
        <artifactId>natives-linux-musl-aarch64</artifactId>
        <version>@LIBDAVE_VERSION@</version>
    </dependency>
    <dependency>
        <groupId>moe.kyokobot.libdave</groupId>
        <artifactId>natives-linux-musl-arm</artifactId>
        <version>@LIBDAVE_VERSION@</version>
    </dependency>
    
    <!-- Windows -->
    <dependency>
        <groupId>moe.kyokobot.libdave</groupId>
        <artifactId>natives-win-x86-64</artifactId>
        <version>@LIBDAVE_VERSION@</version>
    </dependency>
    <dependency>
        <groupId>moe.kyokobot.libdave</groupId>
        <artifactId>natives-win-x86</artifactId>
        <version>@LIBDAVE_VERSION@</version>
    </dependency>
    <dependency>
        <groupId>moe.kyokobot.libdave</groupId>
        <artifactId>natives-win-arm</artifactId>
        <version>@LIBDAVE_VERSION@</version>
    </dependency>
    
    <!-- macOS -->
    <dependency>
        <groupId>moe.kyokobot.libdave</groupId>
        <artifactId>natives-darwin</artifactId>
        <version>@LIBDAVE_VERSION@</version>
    </dependency>
</dependencies>
```

### SBT
```scala
libraryDependencies ++= Seq(
  "com.discord4j" % "discord4j-voice" % "@VERSION@"
  
  // NOTE: Keep only the natives that match your OS and architecture.
  // You don't need to include all of them.

  // Linux (glibc 2.28 / EL8)
  "moe.kyokobot.libdave" % "natives-linux-x86-64" % "@LIBDAVE_VERSION@",

  // Linux (glibc 2.35)
  "moe.kyokobot.libdave" % "natives-linux-x86" % "@LIBDAVE_VERSION@",
  "moe.kyokobot.libdave" % "natives-linux-aarch64" % "@LIBDAVE_VERSION@",
  "moe.kyokobot.libdave" % "natives-linux-arm" % "@LIBDAVE_VERSION@",
  
  // Linux (musl)
  "moe.kyokobot.libdave" % "natives-linux-musl-x86-64" % "@LIBDAVE_VERSION@",
  "moe.kyokobot.libdave" % "natives-linux-musl-x86" % "@LIBDAVE_VERSION@",
  "moe.kyokobot.libdave" % "natives-linux-musl-aarch64" % "@LIBDAVE_VERSION@",
  "moe.kyokobot.libdave" % "natives-linux-musl-arm" % "@LIBDAVE_VERSION@",
  
  // Windows
  "moe.kyokobot.libdave" % "natives-win-x86-64" % "@LIBDAVE_VERSION@",
  "moe.kyokobot.libdave" % "natives-win-x86" % "@LIBDAVE_VERSION@",
  "moe.kyokobot.libdave" % "natives-win-arm" % "@LIBDAVE_VERSION@",
  
  // macOS
  "moe.kyokobot.libdave" % "natives-darwin" % "@LIBDAVE_VERSION@"
)
```

## DAVE Requirements
Discord requires the DAVE voice protocol for standard voice connections as of March 1, 2026.

## Development builds
Please follow our instructions at [Using Jitpack](https://github.com/Discord4J/Discord4J/wiki/Using-Jitpack)

## Example Usage
Check out this [Example voice bot](https://github.com/Discord4J/Discord4J/blob/master/core/src/test/java/discord4j/core/ExampleVoiceBot.java).
