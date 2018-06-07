# Discord4J Rest
The `rest` module provides a low-level HTTP client specifically for Discord which properly handles Discord's [ratelimiting system](https://discordapp.com/developers/docs/topics/rate-limits).

## Installation
### Gradle
```groovy
repositories {
  maven { url  "https://jitpack.io" }
}

dependencies {
  implementation "com.discord4j.discord4j:discord4j-rest:@VERSION@"
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
    <artifactId>discord4j-rest</artifactId>
    <version>@VERSION@</version>
  </dependency>
</dependencies>
```

## Example Usage
```java
final ObjectMapper mapper = new ObjectMapper()
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
                .registerModules(new PossibleModule(), new Jdk8Module());

final SimpleHttpClient httpClient = SimpleHttpClient.builder()
                .defaultHeader("content-type", "application/json")
                .defaultHeader("authorization", "Bot " + token)
                .defaultHeader("user-agent", "DiscordBot(" + url + ", " + version + ")")
                .readerStrategy(new JacksonReaderStrategy<>(mapper))
                .readerStrategy(new EmptyReaderStrategy())
                .writerStrategy(new MultipartWriterStrategy(mapper))
                .writerStrategy(new JacksonWriterStrategy(mapper))
                .writerStrategy(new EmptyWriterStrategy())
                .baseUrl(Routes.BASE_URL)
                .build();

final RestClient restClient = new RestClient(new Router(httpClient));

restClient.getApplicationService().getCurrentApplicationInfo()
                .map(ApplicationInfoResponse::getName)
                .subscribe(name -> System.out.println("My name is " + name));
```