# Discord4J Commands
The `commands` module provides a set of low-level abstractions for implementing commands. 

To use this, command implementations should implement the `CommandProvider` class and users should instantiate the
`CommandBootstrapper` class in order to register `CommandProvider`s for use.

## Installation
### Gradle
```groovy
repositories {
  maven { url  "https://jitpack.io" }
}

dependencies {
  implementation "com.discord4j.discord4j:discord4j-commands:@VERSION@"
}
```
### Maven
```xml
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>com.discord4j.discord4j</groupId>
    <artifactId>discord4j-commands</artifactId>
    <version>@VERSION@</version>
  </dependency>
</dependencies>
```