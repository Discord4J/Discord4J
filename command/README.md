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
### Gradle
```groovy
repositories {
  maven { url 'https://jitpack.io' }
  maven { url 'https://repo.spring.io/milestone' }
}

dependencies {
  implementation 'com.discord4j.discord4j:discord4j-command:@VERSION@'
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
    <artifactId>discord4j-command</artifactId>
    <version>@VERSION@</version>
  </dependency>
</dependencies>
```

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
    public Flux<ProviderContext> provide(MessageCreateEvent context, String commandName, int startIndex, int endIndex) { //Determine which command(s) to use, if any
        return Mono.justOrEmpty(commandName)
                .flatMap(commandName -> {
                    if (commandName.equals("echo")) {
                        return Mono.just(ProviderContext.of(new EchoCommand())); //Handle message with an EchoCommand
                    }
                    //This provider cant handle the message. Returning empty() lets other providers try
                    return Mono.empty();
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
