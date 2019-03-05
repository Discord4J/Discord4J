### WARNING: The following module is highly experimental and prone to drastic API changes between versions. It is not recommended for general use!

# Discord4J Command
The `command` module provides a set of low-level tools for dealing with bot commands. It can be used on its own or
easily serve as a basis of interoperability for higher-level command libraries. 

This module is extremely extensible while still being relatively lightweight. This allows for a very large degree of
freedom, meaning that your cool idea for a command api can be easily implemented as a layer atop this base api.
Additionally, this api allows for commands to interoperate very easily. Interoperation allows for commands to be easily
distributed without an actual bot, meaning that developers can create bot-less "command packs" which can be incorporated
into other users' bots quite simply.

## Installation
Just replace `@VERSION@` with the latest given by ![](https://img.shields.io/maven-central/v/com.discord4j/discord4j-command.svg?style=flat-square)
### Gradle
```groovy
repositories {
  mavenCentral()
}

dependencies {
  implementation 'com.discord4j:discord4j-command:@VERSION@'
}
```
### Maven
```xml
<dependencies>
  <dependency>
    <groupId>com.discord4j</groupId>
    <artifactId>discord4j-command</artifactId>
    <version>@VERSION@</version>
  </dependency>
</dependencies>
```

### SBT
```scala
libraryDependencies ++= Seq(
  "com.discord4j" % "discord4j-command" % "@VERSION@"
)
```

## Development builds
Please follow our instructions at [Using Jitpack](https://github.com/Discord4J/Discord4J/wiki/Using-Jitpack)

## Example usage
Creating a command:
```java
/**
 * This command gets executed on !echo to reply with the same content received.
 */
public class EchoCommand implements Command {
    @Override
    public Mono<Void> execute(MessageCreateEvent event, @Nullable Object context) { //invoked message is !echo
        return Mono.justOrEmpty(event.getMessage().getContent())
                .map(content -> content.substring(content.indexOf(" "))) //Retrieve string to reply with
                .zipWith(event.getMessage().getChannel()) // Get channel to send the reply to
                .flatMap(tuple -> tuple.getT2().createMessage(tuple.getT1())) // Reply
                .then(); //Transform to Mono<Void>
    }
}
```
Preparing your command for use:
```java
/**
 * This provides a connection between your command(s) and the command client.
 */
class MyCommandProvider implements CommandProvider {
    @Override
    public Flux<ProviderContext<Void>> provide(MessageCreateEvent context,
                                               String cmdName,
                                               int startIndex,
                                               int endIndex) {
        return Mono.just(cmdName)
                .map(cmd -> {
                    if (cmd.equalsIgnoreCase("echo")) {
                        return ProviderContext.of(new EchoCommand(startIndex, endIndex));
                    } else {
                        return null;
                    }
                }).flux();
    }
}
```
Connect the command module:
```java
/**
 * Since this api is optional, we must actually hook everything up to our DiscordClient.
 */
public class CommandConnectionExample {
    
    public class SimpleCommandDispatcher extends AbstractCommandDispatcher {
        private final String prefix;
        
        public SimpleCommandDispatcher(String prefix) {
            this.prefix = prefix;
        }
    
        @Override
        protected Publisher<String> getPrefixes(MessageCreateEvent event) {
            return Mono.just(prefix);
        }
    }
    
    public static void setupCommands(DiscordClient client) {
        SimpleCommandDispatcher dispatcher = new SimpleCommandDispatcher("!"); //Handles triggering commands using our ! prefix
        CommandBootstrapper bootstrapper = new CommandBootstrapper(dispatcher); //This mediates all internal logic for commands
        bootstrapper.addProvider(new MyCommandProvider()); //Register our command provider
        bootstrapper.attach(client).subscribe(); //Attach the provider to the client and activate it
    }
}
```
