# Discord4J Common
The `common` module contains base utilities and models useful for other modules.

## Installation
### Gradle
```groovy
repositories {
  maven { url 'https://jitpack.io' }
  maven { url 'https://repo.spring.io/milestone' }
}

dependencies {
  implementation 'com.discord4j.discord4j:discord4j-common:@VERSION@'
}
```
### Maven
```xml
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
  <repository> 
    <id>repository.spring.milestone</id> 
    <url>http://repo.spring.io/milestone</url> 
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>com.discord4j.discord4j</groupId>
    <artifactId>discord4j-common</artifactId>
    <version>@VERSION@</version>
  </dependency>
</dependencies>
```