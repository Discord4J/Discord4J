/*
 *     This file is part of Discord4J.
 *
 *     Discord4J is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Discord4J is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */

package sx.blah.discord;

import org.junit.Test;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.DiscordStatus;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.shard.DisconnectedEvent;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.modules.Configuration;
import sx.blah.discord.util.*;
import sx.blah.discord.util.audio.AudioPlayer;

import java.io.File;
import java.util.StringJoiner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * General testing bot. Also a demonstration of how to use the bot.
 */
public class TestBot {

	private static final String CI_URL = "https://drone.io/github.com/austinv11/Discord4J/";
	private static final long MAX_TEST_TIME = 120000L;

	@Test(timeout = 300000L)
	public void testBot() {
		main(System.getenv("USER"), "CITest");
	}

	/**
	 * Starts the bot. This can be done any place you want.
	 * The main method is for demonstration.
	 *
	 * @param args Command line arguments passed to the program.
	 */
	public static void main(String... args) {
		try {
			if (Discord4J.LOGGER instanceof Discord4J.Discord4JLogger) {
				((Discord4J.Discord4JLogger) Discord4J.LOGGER).setLevel(Discord4J.Discord4JLogger.Level.TRACE);
			}

			Configuration.LOAD_EXTERNAL_MODULES = false; //temp

			boolean isTesting = args[args.length-1].equals("CITest");

			IDiscordClient client = new ClientBuilder().withToken(args[0]).withPingTimeout(1).build();

			client.getDispatcher().registerListener((IListener<DisconnectedEvent>) (event) -> {
				Discord4J.LOGGER.warn("Client disconnected for reason: {}", event.getReason());
			});

			if (isTesting) { //CI Testing
				Discord4J.LOGGER.debug("CI Test Initiated");
				Discord4J.LOGGER.debug("Discord API has a response time of {}ms", DiscordStatus.getAPIResponseTimeForDay());

				for (DiscordStatus.Maintenance maintenance : DiscordStatus.getUpcomingMaintenances()) {
					Discord4J.LOGGER.warn("Discord has upcoming maintenance: {} on {}", maintenance.getName(), maintenance.getStart().toString());
				}

				client.login();

				final AtomicBoolean didTest = new AtomicBoolean(false);
				client.getDispatcher().registerListener(new IListener<ReadyEvent>() {
					@Override
					public void handle(ReadyEvent readyEvent) {
						try {
							//Initialize required data
							final IChannel testChannel = client.getChannelByID(Long.parseUnsignedLong(System.getenv("CHANNEL")));
							final IChannel spoofChannel = client.getChannelByID(Long.parseUnsignedLong(System.getenv("SPOOF_CHANNEL")));
							String buildNumber = System.getenv("BUILD_ID");

							IVoiceChannel channel = client.getVoiceChannels().stream().filter(voiceChannel-> voiceChannel.getName().equalsIgnoreCase("Annoying Shit")).findFirst().orElse(null);
							if (channel != null) {
								channel.join();
								AudioPlayer.getAudioPlayerForGuild(channel.getGuild()).queue(new File("./test.mp3")); //Mono test
								AudioPlayer.getAudioPlayerForGuild(channel.getGuild()).queue(new File("./test.flac")); //Mono test
								AudioPlayer.getAudioPlayerForGuild(channel.getGuild()).queue(new File("./test2.mp3")); //Stereo test
								AudioPlayer.getAudioPlayerForGuild(channel.getGuild()).queue(new File("./test2.flac")); //Stereo test
							}

							//Start testing
							new MessageBuilder(client).withChannel(testChannel).withContent("Initiating Discord4J Unit Tests for Build #"+
									buildNumber, MessageBuilder.Styles.BOLD).build();

							//Clearing spoofbot's mess from before
							synchronized (client) {
								for (IMessage message : spoofChannel.getMessageHistory()) {
									RequestBuffer.request(() -> {
										try {
											message.delete();
										} catch (MissingPermissionsException | DiscordException e) {
											e.printStackTrace();
										}
									});
								}
							}

							//Time to unleash the ai
							SpoofBot spoofBot = new SpoofBot(client, System.getenv("SPOOF"), Long.parseUnsignedLong(System.getenv("SPOOF_CHANNEL")));

							final long now = System.currentTimeMillis();
							new Thread(() -> {
								while (!didTest.get()) {
									if (now+MAX_TEST_TIME <= System.currentTimeMillis()) {
										//Test timer up!
										synchronized (client) {
											try {
												new MessageBuilder(client).withChannel(testChannel).withContent("Success! The build is complete. See the log here: "+CI_URL+buildNumber,
														MessageBuilder.Styles.BOLD).build();
											} catch (RateLimitException | MissingPermissionsException | DiscordException e) {
												e.printStackTrace();
											}
										}
										didTest.set(true);
									}
								}
							}).start();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});

				while (!didTest.get()) {}

			} else { //Dev testing
				client.login();

				client.getDispatcher().registerListener(new IListener<ReadyEvent>() {
					@Override
					public void handle(ReadyEvent event) {
						Discord4J.LOGGER.info("Connected to {} guilds.", event.getClient().getGuilds().size());
					}
				});

				client.getDispatcher().registerListener(new IListener<MessageReceivedEvent>() {
					@Override
					public void handle(MessageReceivedEvent messageReceivedEvent) {
						try {
							IMessage m = messageReceivedEvent.getMessage();
							if (m.getAuthor().equals(client.getApplicationOwner())) {
								if (m.getContent().startsWith(".meme")
										|| m.getContent().startsWith(".nicememe")) {
									try {
										new MessageBuilder(client).appendContent("MEMES REQUESTED:", MessageBuilder.Styles.UNDERLINE_BOLD_ITALICS)
												.appendContent(" http://niceme.me/").withChannel(messageReceivedEvent.getMessage().getChannel())
												.build();
									} catch (RateLimitException | DiscordException | MissingPermissionsException e) {
										e.printStackTrace();
									}
								} else if (m.getContent().startsWith(".clear")) {
									IChannel c = client.getChannelByID(m.getChannel().getLongID());
									if (null != c) {
										c.getMessageHistory().stream().filter(message -> message.getAuthor().getLongID()
												== client.getOurUser().getLongID()).forEach(message -> {
											try {
												Discord4J.LOGGER.debug("Attempting deletion of message {} by \"{}\" ({})", message.getStringID(), message.getAuthor().getName(), message.getContent());
												message.delete();
											} catch (MissingPermissionsException | RateLimitException | DiscordException e) {
												e.printStackTrace();
											}
										});
									}
								} else if (m.getContent().startsWith(".name ")) {
									String s = m.getContent().split(" ", 2)[1];
									try {
										client.changeUsername(s);
										m.reply("is this better?");
									} catch (RateLimitException | MissingPermissionsException | DiscordException e) {
										e.printStackTrace();
									}
								} else if (m.getContent().startsWith(".pm")) {
									try {
										IPrivateChannel channel = client.getOrCreatePMChannel(m.getAuthor());
										new MessageBuilder(client).withChannel(channel).withContent("SUP DUDE").build();
									} catch (Exception e) {
										e.printStackTrace();
									}
								} else if (m.getContent().startsWith(".presence")) {
									client.changePresence(StatusType.IDLE);
								} else if (m.getContent().startsWith(".game")) {
									String game = m.getContent().length() > 6 ? m.getContent().substring(6) : null;
									client.changePresence(StatusType.ONLINE, ActivityType.PLAYING, game);
								} else if (m.getContent().startsWith(".type")) {
									m.getChannel().toggleTypingStatus();
								} else if (m.getContent().startsWith(".invite")) {
									try {
										m.reply("http://discord.gg/"+m.getChannel().createInvite(1800, 0, false, false).getCode());
									} catch (MissingPermissionsException | RateLimitException | DiscordException e) {
										e.printStackTrace();
									}
								} else if (m.getContent().startsWith(".avatar")) {
									try {
										if (m.getContent().split(" ").length > 1) {
											String url = m.getContent().split(" ")[1];
											client.changeAvatar(Image.forUrl(url.substring(url.lastIndexOf('.')), url));
										} else {
											client.changeAvatar(Image.defaultAvatar());
										}
									} catch (Exception e) {
										e.printStackTrace();
									}
								} else if (m.getContent().startsWith(".permissions")) {
									if (m.getMentions().size() < 1)
										return;
									StringJoiner roleJoiner = new StringJoiner(", ");
									StringJoiner permissionsJoiner = new StringJoiner(", ");
									for (IRole role : m.getMentions().get(0).getRolesForGuild(m.getChannel().getGuild())) {
										Discord4J.LOGGER.info("{}", role.getStringID());
										for (Permissions permissions : role.getPermissions()) {
											permissionsJoiner.add(permissions.toString());
										}
										roleJoiner.add(role.getName()+" ("+permissionsJoiner.toString()+")");
										permissionsJoiner = new StringJoiner(", ");
									}
									try {
										Discord4J.LOGGER.info("{}", m.getAuthor().getStringID());
										m.reply("This user has the following roles and permissions: "+roleJoiner.toString());
									} catch (MissingPermissionsException | RateLimitException | DiscordException e) {
										e.printStackTrace();
									}
								} else if (m.getContent().startsWith(".join")) {
									IVoiceChannel channel = m.getGuild().getVoiceChannelsByName(m.getContent().split(" ")[1]).get(0);
									channel.join();
								} else if (m.getContent().startsWith(".leave")) {
									IVoiceChannel channel = m.getGuild().getVoiceChannelsByName(m.getContent().split(" ")[1]).get(0);
									channel.leave();
								} else if (m.getContent().startsWith(".play")) {
									AudioPlayer player = AudioPlayer.getAudioPlayerForGuild(m.getGuild());
									player.queue(new File("./test.mp3"));
									player.queue(new File("./test.flac"));
									player.queue(new File("./test2.mp3"));
									player.queue(new File("./test2.flac"));
									// player.queue(new URL("https://github.com/austinv11/Discord4J/raw/master/test.mp3"));
									// player.queue(new URL("https://github.com/austinv11/Discord4J/raw/master/test2.mp3"));
								} else if (m.getContent().startsWith(".pause")) {
									AudioPlayer player = AudioPlayer.getAudioPlayerForGuild(m.getGuild());
									player.setPaused(true);
								} else if (m.getContent().startsWith(".resume")) {
									AudioPlayer player = AudioPlayer.getAudioPlayerForGuild(m.getGuild());
									player.setPaused(false);
								} else if (m.getContent().startsWith(".volume")) {
									AudioPlayer player = AudioPlayer.getAudioPlayerForGuild(m.getGuild());
									player.setVolume(Float.parseFloat(m.getContent().split(" ")[1]));
								} else if (m.getContent().startsWith(".stop")) {
									client.getConnectedVoiceChannels().stream().filter((IVoiceChannel channel) -> channel.getGuild().equals(m.getGuild())).findFirst().ifPresent(IVoiceChannel::leave);
								} else if (m.getContent().startsWith(".skip")) {
									AudioPlayer player = AudioPlayer.getAudioPlayerForGuild(m.getGuild());
									player.skip();
								} else if (m.getContent().startsWith(".toggleloop")) {
									AudioPlayer player = AudioPlayer.getAudioPlayerForGuild(m.getGuild());
									player.setLoop(!player.isLooping());
								} else if (m.getContent().startsWith(".rewind")) {
									AudioPlayer player = AudioPlayer.getAudioPlayerForGuild(m.getGuild());
									player.getCurrentTrack().rewind(Long.parseLong(m.getContent().split(" ")[1]));
								} else if (m.getContent().startsWith(".forward")) {
									AudioPlayer player = AudioPlayer.getAudioPlayerForGuild(m.getGuild());
									player.getCurrentTrack().fastForward(Long.parseLong(m.getContent().split(" ")[1]));
								} else if (m.getContent().startsWith(".shuffle")) {
									AudioPlayer player = AudioPlayer.getAudioPlayerForGuild(m.getGuild());
									player.shuffle();
								} else if (m.getContent().startsWith(".spam")) {
									new Timer().scheduleAtFixedRate(new TimerTask() {
										@Override
										public void run() {
											RequestBuffer.request(() -> {
												try {
													return m.getChannel().sendMessage("spam");
												} catch (MissingPermissionsException | DiscordException e) {
													e.printStackTrace();
												}
												return null;
											});
										}
									}, 0, 50);
								} else if (m.getContent().startsWith(".move ")) {
									String target = m.getContent().split(" ")[1];
									try {
										client.getOurUser().moveToVoiceChannel(m.getGuild().getVoiceChannels().stream()
												.filter((IVoiceChannel channel) -> channel.getName().equals(target)).findFirst().orElseGet(null));
									} catch (DiscordException | RateLimitException | MissingPermissionsException e) {
										e.printStackTrace();
									}
								} else if (m.getContent().startsWith(".logout")) {
									client.logout();
								} else if (m.getContent().startsWith(".test")) {
									test(m);
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					//Used for convenience in testing
					private void test(IMessage message) throws Exception {
						message.reply(message.getClient().fetchUser(Long.parseUnsignedLong(message.getContent().split(" ")[1])).mention());
					}
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
