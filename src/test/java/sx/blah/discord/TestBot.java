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

import org.json.simple.parser.ParseException;
import sx.blah.discord.handle.IListener;
import sx.blah.discord.handle.impl.events.InviteReceivedEvent;
import sx.blah.discord.handle.impl.events.MessageDeleteEvent;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.Channel;
import sx.blah.discord.handle.obj.Invite;
import sx.blah.discord.handle.obj.Message;
import sx.blah.discord.handle.obj.PrivateChannel;
import sx.blah.discord.util.MessageBuilder;

import java.io.IOException;

/**
 * @author qt
 * @since 8:00 PM 16 Aug, 2015
 * Project: DiscordAPI
 * <p>
 * General testing bot. Also a demonstration of how to use the bot.
 */
public class TestBot {

	/**
	 * Starts the bot. This can be done any place you want.
	 * The main method is for demonstration.
	 *
	 * @param args Command line arguments passed to the program.
	 */
	public static void main(String... args) {
		try {
			DiscordClient.get().login(args[0] /* username */, args[1] /* password */);

			DiscordClient.get().getDispatcher().registerListener(new IListener<MessageReceivedEvent>() {
				@Override public void receive(MessageReceivedEvent messageReceivedEvent) {
					Message m = messageReceivedEvent.getMessage();
					if (m.getContent().startsWith(".meme")
							|| m.getContent().startsWith(".nicememe")) {
							new MessageBuilder().appendContent("MEMES REQUESTED:", MessageBuilder.Styles.UNDERLINE_BOLD_ITALICS)
                                    .appendContent(" http://niceme.me/").withChannel(messageReceivedEvent.getMessage().getChannel())
                                    .build();
					} else if (m.getContent().startsWith(".clear")) {
						Channel c = DiscordClient.get().getChannelByID(m.getChannel().getID());
						if (null != c) {
							c.getMessages().stream().filter(message -> message.getAuthor().getID()
									.equalsIgnoreCase(DiscordClient.get().getOurUser().getID())).forEach(message -> {
								try {
									Discord4J.logger.debug("Attempting deletion of message {} by \"{}\" ({})", message.getID(), message.getAuthor().getName(), message.getContent());
									DiscordClient.get().deleteMessage(message.getID(), message.getChannel().getID());
								} catch (IOException e) {
									Discord4J.logger.error("Couldn't delete message {} ({}).", message.getID(), e.getMessage());
								}
							});
						}
					} else if (m.getContent().startsWith(".name ")) {
						String s = m.getContent().split(" ", 2)[1];
						try {
							DiscordClient.get().changeAccountInfo(s, "", "");
							m.reply("is this better?");
						} catch (ParseException | IOException e) {
							e.printStackTrace();
						}
					} else if(m.getContent().startsWith(".pm")) {
                        try {
                            PrivateChannel channel = DiscordClient.get().getOrCreatePMChannel(m.getAuthor());
                            new MessageBuilder().withChannel(channel).withContent("SUP DUDE").build();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
				}
			});

			DiscordClient.get().getDispatcher().registerListener(new IListener<InviteReceivedEvent>() {
				@Override public void receive(InviteReceivedEvent event) {
					Invite invite = event.getInvite();
					try {
						Invite.InviteResponse response = invite.details();
						event.getMessage().reply(String.format("you've invited me to join #%s in the %s guild!", response.getChannelName(), response.getGuildName()));
                        invite.accept();
                        DiscordClient.get().sendMessage(String.format("Hello, #%s and the \\\"%s\\\" guild! I was invited by %s!",
                                        response.getChannelName(), response.getGuildName(), event.getMessage().getAuthor()),
								response.getChannelID());
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			});

			DiscordClient.get().getDispatcher().registerListener(new IListener<MessageDeleteEvent>() {
				@Override public void receive(MessageDeleteEvent event) {
					try {
						event.getMessage().reply("you said, \\\"" + event.getMessage().getContent() + "\\\"");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
