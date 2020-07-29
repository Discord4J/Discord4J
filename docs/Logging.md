## Implementation
Discord4J utilizes logging throughout their modules that will be forwarded to your implementation of choice, according to Reactor [``Loggers``](https://projectreactor.io/docs/core/release/api/reactor/util/Loggers.html) class which picks up common logging frameworks at startup and configures logging appropriately.

- If you have any [SLF4J](https://www.slf4j.org/) implementation available, it will be picked up first.
- As a fallback, it will log to the console, using `System.err` for the `WARN` and `ERROR` log levels and `System.out` for the rest.
- If you prefer to log to JDK `java.util.logging` you must set the `reactor.logging.fallback` system property to `JDK`, For example, if running from the command line:
```
-Dreactor.logging.fallback=JDK
```

## Logging a Stream
You have the ability to log events in a reactive sequence, like those coming from Discord4J. The `log()` operator is able to do that, peeking at every signal that goes through a sequence. You can learn more about this operator on the Reactor reference guide [here](http://projectreactor.io/docs/core/release/reference/#_logging_a_stream).

## Configuration

### SLF4J Simple
SLF4J Simple is a basic implementation that outputs INFO and higher logging directly to System.err. It's easy to use and requires no additional files. Check it out [here](https://mvnrepository.com/artifact/org.slf4j/slf4j-simple/1.8.0-beta2).

### Logback
[Logback](https://logback.qos.ch/) is an SLF4J implementation you can use with Discord4J to further configure logging. The following is an example to use it. First add [this dependency](https://search.maven.org/artifact/ch.qos.logback/logback-classic/1.2.3/jar) to your project. Then create a file under `src/main/resources` named `logback.xml` with the following content:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration scan="true">
    <!-- You can configure per-logger level at this point -->
    <!-- This set of preconfigured loggers is good if you want to have a DEBUG level as baseline -->
    <logger name="io.netty" level="INFO"/>
    <logger name="reactor" level="INFO"/>

    <!-- Display the logs in your console with the following format -->
    <!-- You can learn more about this here: https://logback.qos.ch/manual/layouts.html#ClassicPatternLayout -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <!-- Log to a file as well, including size and time based rolling -->
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>logs/d4j.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <maxFileSize>100MB</maxFileSize>
            <maxHistory>90</maxHistory>
        </rollingPolicy>
        <encoder>
            <charset>UTF-8</charset>
            <Pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %-40.40logger{39} : %msg%n</Pattern>
        </encoder>
        <prudent>true</prudent>
    </appender>

    <!-- Avoid blocking while logging to file by wrapping our file appender with async capabilities -->
    <appender name="ASYNC" class="ch.qos.logback.classic.AsyncAppender">
        <queueSize>512</queueSize>
        <appender-ref ref="FILE"/>
    </appender>

    <!-- Here you can set the base logger level. If DEBUG is too chatty for you, you can use INFO -->
    <!-- Possible options are: ALL, TRACE, DEBUG, INFO, WARN, ERROR, OFF -->
    <root level="DEBUG">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="ASYNC"/>
    </root>
</configuration>
```

### Log4J2
[Log4J2](https://logging.apache.org/log4j/2.x/) can also work with Discord4J using an SLF4J adapter. To begin please add [log4j-slf4j18-impl](https://search.maven.org/artifact/org.apache.logging.log4j/log4j-slf4j18-impl/2.13.0/jar) as dependency.

## Available loggers
Discord4J has a logger structure that differs from v2, where you can tweak at the logger level the verbosity you prefer.

If you want to reduce the logging produced by websocket data you can use the following in your `logback.xml` file:
```xml
<logger name="discord4j.gateway" level="INFO"/>
```

The following table shows the levels you can set each logger to obtain your preferred details. They work on a hierarchy basis therefore setting a level to `discord4j.rest` affects every logger under it like `discord4j.rest.traces`.

### v3.0 Loggers

| Logger | Level | Description |
| ------------- | ------------- | ------------- |
| `discord4j.core` | DEBUG | Details about core internal processes |
| `discord4j.dispatch` | DEBUG | Event dispatcher received Dispatch |
| `discord4j.events` | DEBUG | Event dispatcher emitted Events |
| `discord4j.rest.http` | DEBUG | HTTP client usage |
| `discord4j.rest.traces` | TRACE | HTTP client requests, response times and rate limits |
| `discord4j.rest.request` | TRACE | HTTP client request details |
| `discord4j.rest.response` | TRACE | HTTP client response details |
| `discord4j.gateway.client` | DEBUG | Heartbeat timing details |
| `discord4j.gateway.client` | INFO | Gateway lifecycle events, like connections and reconnections |
| `discord4j.gateway.client` | ERROR | Gateway errors, including "expected" ones |
| `discord4j.gateway.inbound` | DEBUG | Gateway received data |
| `discord4j.gateway.outbound` | DEBUG | Gateway sent data |
| `discord4j.gateway.receiver` | TRACE | Raw JSON received from Gateway |
| `discord4j.gateway.sender` | TRACE | Raw JSON sent to Gateway |
| `discord4j.gateway.zip` | TRACE | Gateway reactive signals |
| `reactor` | DEBUG | Low level details for Reactor operations |
| `reactor.retry` | DEBUG | Details about Websocket reconnections and API request retrying after errors |
| `io.netty` | DEBUG | Low level details of underlying Netty implementation |

### Advanced filtering using Logback
If you're looking to filter out certain gateway event types, you could copy [this custom TurboFilter](https://github.com/Discord4J/Discord4J/blob/master/core/src/test/java/discord4j/core/logback/GatewayEventFilter.java) to your project or as a start point for your own.

And then apply it to your `logback.xml`, for example the following would only show created message events:
```xml
<configuration>
    <turboFilter class="discord4j.core.logback.GatewayEventFilter">
        <Include>MESSAGE_CREATE</Include>
    </turboFilter>
    ...
```
While this one would show all events except presence updates:
```xml
<configuration>
    <turboFilter class="discord4j.core.logback.GatewayEventFilter">
        <Exclude>PRESENCE_UPDATE</Exclude>
    </turboFilter>
    ...
```
The value like `PRESENCE_UPDATE` must match from the ones in [this](https://github.com/Discord4J/Discord4J/blob/master/gateway/src/main/java/discord4j/gateway/json/dispatch/EventNames.java) file.

For complete control over what you want to filter in a declarative manner, use JaninoEvaluator. First add [janino](https://search.maven.org/artifact/org.codehaus.janino/janino/3.0.10/jar) as a dependency and then apply the following to your `logback.xml`:

```xml
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <filter class="ch.qos.logback.core.filter.EvaluatorFilter">
            <evaluator>
                <expression>return formattedMessage.contains("PresenceUpdate");</expression>
            </evaluator>
            <OnMismatch>NEUTRAL</OnMismatch>
            <OnMatch>DENY</OnMatch>
        </filter>
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
```

For more details of what you can put under the `<expression>` attribute, see [this](https://logback.qos.ch/manual/filters.html#JaninoEventEvaluator) documentation page.