# Discord4J Gateway
The `gateway` module provides a low-level WebSocket client for interacting with the [Discord Gateway](https://discordapp.com/developers/docs/topics/gateway).

## Installation
Just replace `@VERSION@` with the latest given by ![](https://img.shields.io/maven-central/v/com.discord4j/discord4j-gateway.svg?style=flat-square)
### Gradle
```groovy
dependencies {
  implementation 'com.discord4j:discord4j-gateway:@VERSION@'
}
```
### Maven
```xml
<dependencies>
  <dependency>
    <groupId>com.discord4j</groupId>
    <artifactId>discord4j-gateway</artifactId>
    <version>@VERSION@</version>
  </dependency>
</dependencies>
```

### SBT
```scala
libraryDependencies ++= Seq(
  "com.discord4j" % "discord4j-gateway" % "@VERSION@"
)
```

## Development builds
Please follow our instructions at [Using Jitpack](https://github.com/Discord4J/Discord4J/wiki/Using-Jitpack)

## Example Usage
```java
ObjectMapper mapper = getMapper();
PayloadReader reader = new JacksonPayloadReader(mapper);
PayloadWriter writer = new JacksonPayloadWriter(mapper);
RetryOptions retryOptions = new RetryOptions(Duration.ofSeconds(5), Duration.ofSeconds(120),
        Integer.MAX_VALUE, Schedulers.elastic());
GatewayClient gatewayClient = new DefaultGatewayClient(HttpClient.create(),
        reader, writer, retryOptions, token,
        new IdentifyOptions(0, 1, null), null,
        new SimpleBucket(1, Duration.ofSeconds(6)));

gatewayClient.dispatch().subscribe(dispatch -> {
    if (dispatch instanceof Ready) {
        System.out.println("Test received READY!");
    }
});

FluxSink<GatewayPayload<?>> outboundSink = gatewayClient.sender();

gatewayClient.dispatch().ofType(MessageCreate.class)
        .subscribe(message -> {
            String content = message.getContent();
            if ("!close".equals(content)) {
                gatewayClient.close(false);
            } else if ("!retry".equals(content)) {
                gatewayClient.close(true);
            }
        });

gatewayClient.execute(gatewayUrl).block();
```
