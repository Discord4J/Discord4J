# Discord4J Commands
The `commands` module provides a set of low-level abstractions for implementing commands. This module provides a 
suitable API for both users trying to add commands to their bot and for users trying to create a high-level command
abstraction.

To use this, command implementations should implement the `CommandProvider` class and users should instantiate the
`CommandBootstrapper` class in order to register `CommandProvider`s for use.

## Why should I use this module?
This module is extremely extensible while still being relatively lightweight. This allows for a very large degree of
freedom, meaning that your cool idea for a command api can still be easily implemented as a layer atop this base api.
Additionally, this api allows for commands to interoperate very easily. Interoperation allows for commands to be easily
distributed without an actual bot, meaning that developers can create bot-less "command packs" which can be incorporated
into other users' bots quite simply.

## Example usage
Creating a command:
```java
/**
 * This command gets executed on !echo to reply with the same content received.
 */
public class EchoCommand implements Command {
    @Override
    public Mono<Void> execute(MessageCreateEvent event) { //This is invoked when !echo has been received
        return Mono.justOrEmpty(event.getMessage().getContent())
                .map(content -> content.substring(content.indexOf(" "))) //Retrieve string to reply with
                .zipWith(event.getMessage().getChannel(), (content, channel) -> channel.createMessage(content)) //Reply
                .then(); //Transform to Mono<Void>
    }
}
```
Preparing your command for use:
```java
/**
 * This provides a connection between your command(s) and the command client.
 */
public class MyCommandProvider implements CommandProvider {
    @Override
    public Mono<? extends Command> provide(MessageCreateEvent context) { //Determines how to route a message to a command
        return Mono.justOrEmpty(context.getMessage()
                    .getContent()
                    .filter(s -> s.startsWith("!")) //Look for our command prefix
                    .map(s -> s.replaceFirst("!", "").split(" ")[0]) //Strip the prefix and get the command name
                    .map(cmd -> {
                        if (cmd.equalsIgnoreCase("echo")) { //Our command!
                            return new EchoCommand();
                        } else { //Uh oh, we can't handle this
                            throw new CommandException("I can't handle your input!"); //Signals the api to report this message as a user-friendly error
                            //return null; //If this Mono returned as empty, this provider would actually be skipped over, 
                            // allowing for other potential providers to provide commands
                        }
                    })
                );
    }
}
```
Connect the command module:
```java
/**
 * Since this api is optional, we must actually hook everything up to our DiscordClient.
 */
public class CommandConnectionExample {
    public static void setupCommands(DiscordClient client) {
        CommandBootstrapper bootstrapper = new CommandBootstrapper(); //This mediates all the internal logic for commands
        bootstrapper.addCommandProvider(new MyCommandProvider()); //Register our command provider
        bootstrapper.attach(client).subscribe(); //Attach the provider to the client and activate it
    }
}
```

## Installation
### Gradle
```groovy
repositories {
  maven { url  "https://jitpack.io" }
}

dependencies {
  implementation "com.discord4j.discord4j:discord4j-command:@VERSION@"
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
    <artifactId>discord4j-command</artifactId>
    <version>@VERSION@</version>
  </dependency>
</dependencies>
```