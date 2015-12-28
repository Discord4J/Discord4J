/*
 * Discord4J - Unofficial wrapper for Discord API
 * Copyright (c) 2015
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package sx.blah.discord;

import org.junit.Test;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.DiscordClient;
import sx.blah.discord.handle.IListener;
import sx.blah.discord.handle.impl.events.InviteReceivedEvent;
import sx.blah.discord.handle.impl.events.MessageDeleteEvent;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.Channel;
import sx.blah.discord.handle.obj.Invite;
import sx.blah.discord.handle.obj.Message;
import sx.blah.discord.handle.obj.PrivateChannel;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.Presences;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author qt
 * @since 8:00 PM 16 Aug, 2015
 * Project: DiscordAPI
 * <p>
 * General testing bot. Also a demonstration of how to use the bot.
 */
public class TestBot {
	
	private static final String CI_URL = "https://drone.io/github.com/austinv11/Discord4J/";
	
	@Test(timeout = 60000L)
	public void testBot() {
		main(System.getenv("USER"), System.getenv("PSW"), "CITest");
	}
	
	/**
	 * Starts the bot. This can be done any place you want.
	 * The main method is for demonstration.
	 *
	 * @param args Command line arguments passed to the program.
	 */
	public static void main(String... args) {
		try {
			DiscordClient client = new ClientBuilder().withLogin(args[0] /* username */, args[1] /* password */).login();

			if (args.length > 2) { //CI Testing
				Discord4J.logger.debug("CI Test Initiated");
				final AtomicBoolean didTest = new AtomicBoolean(false);
				client.getDispatcher().registerListener(new IListener<ReadyEvent>() {
					@Override
					public void receive(ReadyEvent messageReceivedEvent) {
						try {
							Invite testInvite = new Invite(client, System.getenv("INVITE").replace("https://discord.gg/", ""));
							Invite.InviteResponse response = testInvite.details();
							Channel testChannel = client.getChannelByID(response.getChannelID());
							String buildNumber = System.getenv("BUILD_ID");
							
							new MessageBuilder().withChannel(testChannel).withContent("Initiating Discord4J Unit Tests for Build #"+
									buildNumber, MessageBuilder.Styles.BOLD).build(client);
							
							//TODO: Real unit tests
							
							new MessageBuilder().withChannel(testChannel).withContent("Success! The build is complete. See the log here: "+
									CI_URL+buildNumber, MessageBuilder.Styles.BOLD).build(client);
							didTest.set(true);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
				
				while (!didTest.get()) {};
				
			} else { //Dev testing
				client.getDispatcher().registerListener(new IListener<MessageReceivedEvent>() {
					@Override
					public void receive(MessageReceivedEvent messageReceivedEvent) {
						Message m = messageReceivedEvent.getMessage();
						if (m.getContent().startsWith(".meme")
								|| m.getContent().startsWith(".nicememe")) {
							new MessageBuilder().appendContent("MEMES REQUESTED:", MessageBuilder.Styles.UNDERLINE_BOLD_ITALICS)
									.appendContent(" http://niceme.me/").withChannel(messageReceivedEvent.getMessage().getChannel())
									.build(client);
						} else if (m.getContent().startsWith(".clear")) {
							Channel c = client.getChannelByID(m.getChannel().getID());
							if (null != c) {
								c.getMessages().stream().filter(message->message.getAuthor().getID()
										.equalsIgnoreCase(client.getOurUser().getID())).forEach(message->{
									try {
										Discord4J.logger.debug("Attempting deletion of message {} by \"{}\" ({})", message.getID(), message.getAuthor().getName(), message.getContent());
										client.deleteMessage(message.getID(), message.getChannel().getID());
									} catch (IOException e) {
										Discord4J.logger.error("Couldn't delete message {} ({}).", message.getID(), e.getMessage());
									}
								});
							}
						} else if (m.getContent().startsWith(".name ")) {
							String s = m.getContent().split(" ", 2)[1];
							try {
								client.changeAccountInfo(s, "", "");
								m.reply(client, "is this better?");
							} catch (IOException e) {
								e.printStackTrace();
							}
						} else if (m.getContent().startsWith(".pm")) {
							try {
								PrivateChannel channel = client.getOrCreatePMChannel(m.getAuthor());
								new MessageBuilder().withChannel(channel).withContent("SUP DUDE").build(client);
							} catch (Exception e) {
								e.printStackTrace();
							}
						} else if (m.getContent().startsWith(".presence")) {
							client.updatePresence(!client.getOurUser().getPresence().equals(Presences.IDLE),
									client.getOurUser().getGame());
						} else if (m.getContent().startsWith(".game")) {
							String game = m.getContent().length() > 6 ? m.getContent().substring(6) : null;
							client.updatePresence(client.getOurUser().getPresence().equals(Presences.IDLE),
									Optional.ofNullable(game));
						}
					}
				});
				
				client.getDispatcher().registerListener(new IListener<InviteReceivedEvent>() {
					@Override
					public void receive(InviteReceivedEvent event) {
						Invite invite = event.getInvite();
						try {
							Invite.InviteResponse response = invite.details();
							event.getMessage().reply(client, String.format("you've invited me to join #%s in the %s guild!", response.getChannelName(), response.getGuildName()));
							invite.accept();
							client.sendMessage(String.format("Hello, #%s and the \\\"%s\\\" guild! I was invited by %s!",
									response.getChannelName(), response.getGuildName(), event.getMessage().getAuthor()),
									response.getChannelID());
						} catch (Exception e) {
							e.printStackTrace();
						}
						
					}
				});
				
				client.getDispatcher().registerListener(new IListener<MessageDeleteEvent>() {
					@Override
					public void receive(MessageDeleteEvent event) {
						try {
							event.getMessage().reply(client, "you said, \\\""+event.getMessage().getContent()+"\\\"");
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
