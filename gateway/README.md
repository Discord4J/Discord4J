# Discord4J Gateway
The `gateway` module provides a low-level WebSocket client for interacting with the [Discord Gateway](https://discordapp.com/developers/docs/topics/gateway).

## Installation
Just replace `@VERSION@` with the latest given by ![](https://img.shields.io/maven-central/v/com.discord4j/discord4j-gateway.svg?style=flat-square)
### Gradle
```groovy
repositories {
  jcenter()
}

dependencies {
  implementation 'com.discord4j:discord4j-gateway:@VERSION@'
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
    <groupId>com.discord4j</groupId>
    <artifactId>discord4j-gateway</artifactId>
    <version>@VERSION@</version>
  </dependency>
</dependencies>
```

## Development builds
Please follow our instructions at [Using Jitpack](https://github.com/Discord4J/Discord4J/wiki/Using-Jitpack)

## Example Usage
```java
PayloadReader reader = new JacksonPayloadReader(mapper);
PayloadWriter writer = new JacksonPayloadWriter(mapper);
RetryOptions retryOptions = new RetryOptions(Duration.ofSeconds(5), Duration.ofSeconds(120), Integer.MAX_VALUE);

GatewayClient gatewayClient = new GatewayClient(reader, writer, retryOptions, "token", new IdentifyOptions())

final Map<String, Object> parameters = new HashMap<>(3);
parameters.put("compress", "zlib-stream");
parameters.put("encoding", "json");
parameters.put("v", 6);

gatewayClient.execute("gateway url", parameters).block();
```