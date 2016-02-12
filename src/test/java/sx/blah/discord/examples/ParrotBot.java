package sx.blah.discord.examples;

import sx.blah.discord.api.DiscordException;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.MissingPermissionsException;
import sx.blah.discord.handle.EventDispatcher;
import sx.blah.discord.handle.IListener;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.HTTP429Exception;
import sx.blah.discord.util.MessageBuilder;

/**
 * This is a simple example to demonstrate event listening and message sending.
 * It will repeat everything said in a channel.
 */
public class ParrotBot extends BaseBot implements IListener<MessageReceivedEvent> {

	public ParrotBot(IDiscordClient discordClient) {
		super(discordClient);
		EventDispatcher dispatcher = this.client.getDispatcher(); //Gets the client's event dispatcher
		dispatcher.registerListener(this); //Registers the event listener
	}

	@Override
	public void handle(MessageReceivedEvent event) {
		IMessage message = event.getMessage(); //Gets the message from the event object NOTE: This is not the content of the message, but the object itself
		IChannel channel = message.getChannel(); //Gets the channel in which this message was sent.
		try {
			//Builds (sends) and new message in the channel that the original message was sent with the content of the original message.
			new MessageBuilder(this.client).withChannel(channel).withContent(message.getContent()).build();
		} catch (HTTP429Exception e) { //HTTP429Exception thrown. The bot is sending messages too quickly!
			System.err.print("Sending messages too quickly!");
			e.printStackTrace();
		} catch (DiscordException e) { //DiscordException thrown. Many possibilities.
			System.err.print(e.getErrorMessage()); //Print the error message sent by Discord
			e.printStackTrace();
		} catch (MissingPermissionsException e) { //MissingPermissionsException thrown. The bot doesn't have permission to send the message!
			System.err.print("Missing permissions for channel!");
			e.printStackTrace();
		}
	}
}
