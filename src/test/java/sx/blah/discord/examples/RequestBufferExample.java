package sx.blah.discord.examples;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.RequestBuffer;

/**
 * This demonstrates the {@link sx.blah.discord.util.RequestBuffer} which can be used to automatically retry
 * rate-limited requests.
 */
public class RequestBufferExample extends BaseBot {

	public RequestBufferExample(IDiscordClient client) {
		super(client);
		EventDispatcher dispatcher = client.getDispatcher(); // Gets the client's event dispatcher
		dispatcher.registerListener(new RequestBufferListener()); // Registers the event listener
	}

	/**
	 * This class will have its handle() method called when a ReadyEvent is dispatched by Discord4J.
	 *
	 * @see ReadyBot
	 */
	public static class RequestBufferListener implements IListener<MessageReceivedEvent> {

		@Override
		public void handle(MessageReceivedEvent event) {
			IMessage messageObj = event
					.getMessage(); // Gets the IMessage object for the event (the message that triggered this event)

			// If the message starts with "!seven"
			if (messageObj.getContent().startsWith("!seven")) {
				/*

			 	What will happen is that the first five messages will send,
			 	and then you will be rate-limited for a few seconds.

			 	RequestBuffer will automatically do the waiting for you!

			 	*/

				for (int i = 0; i < 7; i++) {
					int finalI = i; // Copy the index to a temp variable for use in the lambda
					// Request to send a message 7 times
					RequestBuffer.request(() -> event.getChannel().sendMessage("message " + (finalI + 1)));
				}
			} else if (messageObj.getContent().startsWith("!edit")) {
				/*

			 	What will happen is that the bot will send a message, and immediately edit it.
			 	However, there's a slight rate-limit on sending/editing messages and you risk a RateLimitException.

			 	RequestBuffer's request method can return a RequestFuture<T>, which acts like a Future<T>, so calling
			 	.get() on the future will block until complete.

			 	*/

				// Set up the editing request
				RequestBuffer.request(() -> {
					// This makes a request to send a message, and then return it
					RequestBuffer.request(() -> {
						return event.getChannel().sendMessage("Before the edit");
					}).get() // Block until the message is sent
							.edit("Edited message!"); // Edit it!
				});
			}


		}
	}
}
