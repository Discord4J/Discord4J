# Discord4J Rest
The `rest` module provides a low-level HTTP client specifically for Discord which properly handles Discord's [ratelimiting system](https://discordapp.com/developers/docs/topics/rate-limits).

## Installation
### Gradle
```groovy
repositories {
  maven { url 'https://jitpack.io' }
  maven { url 'https://repo.spring.io/milestone' }
}

dependencies {
  implementation 'com.discord4j.discord4j:discord4j-rest:@VERSION@'
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
    <artifactId>discord4j-rest</artifactId>
    <version>@VERSION@</version>
  </dependency>
</dependencies>
```

## Example Usage
```java
final ObjectMapper mapper = new ObjectMapper()
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                .addHandler(new UnknownPropertyHandler(ignoreUnknownJsonKeys))
                .registerModules(new PossibleModule(), new Jdk8Module());

HttpHeaders defaultHeaders = new DefaultHttpHeaders();
defaultHeaders.add(HttpHeaderNames.CONTENT_TYPE, "application/json");
defaultHeaders.add(HttpHeaderNames.AUTHORIZATION, "Bot " + token);
defaultHeaders.add(HttpHeaderNames.USER_AGENT, "DiscordBot(https://discord4j.com, v3)");
HttpClient httpClient = HttpClient.create().baseUrl(Routes.BASE_URL).compress(true);

DiscordWebClient webClient = new DiscordWebClient(httpClient, defaultHeaders,
        ExchangeStrategies.withJacksonDefaults(mapper));

final RestClient restClient = new RestClient(new Router(httpClient), Schedulers.elastic());

restClient.getApplicationService().getCurrentApplicationInfo()
                .map(ApplicationInfoResponse::getName)
                .subscribe(name -> System.out.println("My name is " + name));
```
