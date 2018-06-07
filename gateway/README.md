# Discord4J Gateway
The `gateway` module provides a low-level WebSocket client for interacting with the [Discord Gateway](https://discordapp.com/developers/docs/topics/gateway).

## Installation
```groovy
repositories {
  maven { url  "https://jitpack.io" }
}

dependencies {
  implementation "com.discord4j.discord4j:discord4j-gateway:@VERSION@"
}
```

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