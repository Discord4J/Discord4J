package sx.blah.discord;

import org.junit.Test;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.DiscordStatus;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.api.internal.DiscordClientImpl;
import sx.blah.discord.api.internal.json.responses.RateLimitResponse;
import sx.blah.discord.handle.impl.events.*;
import sx.blah.discord.handle.impl.obj.Invite;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.modules.Configuration;
import sx.blah.discord.util.*;
import sx.blah.discord.util.audio.AudioPlayer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.*;
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

			IDiscordClient client;

			if ((isTesting && args.length > 2) || (!isTesting && args.length > 1))
				client = new ClientBuilder().withLogin(args[0] /* username */, args[1] /* password */).build();
			else
				client = new ClientBuilder().withToken(args[0]).build();

			client.getDispatcher().registerListener((IListener<DiscordDisconnectedEvent>) (event) -> {
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
							final IChannel testChannel = client.getChannelByID(System.getenv("CHANNEL"));
							final IChannel spoofChannel = client.getChannelByID(System.getenv("SPOOF_CHANNEL"));
							String buildNumber = System.getenv("BUILD_ID");

							IVoiceChannel channel = client.getVoiceChannels().stream().filter(voiceChannel-> voiceChannel.getName().equalsIgnoreCase("Annoying Shit")).findFirst().orElse(null);
							if (channel != null) {
								channel.join();
								AudioPlayer.getAudioPlayerForGuild(channel.getGuild()).queue(new File("./test.mp3")); //Mono test
								AudioPlayer.getAudioPlayerForGuild(channel.getGuild()).queue(new File("./test2.mp3")); //Stereo test
							}

							//Start testing
							new MessageBuilder(client).withChannel(testChannel).withContent("Initiating Discord4J Unit Tests for Build #"+
									buildNumber, MessageBuilder.Styles.BOLD).build();

							//Clearing spoofbot's mess from before
							synchronized (client) {
								for (IMessage message : spoofChannel.getMessages()) {
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
							SpoofBot spoofBot = new SpoofBot(client, System.getenv("SPOOF"), System.getenv("SPOOF_CHANNEL"));

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
							if (m.getContent().startsWith(".meme")
									|| m.getContent().startsWith(".nicememe")) {
								try {
									new MessageBuilder(client).appendContent("MEMES REQUESTED:", MessageBuilder.Styles.UNDERLINE_BOLD_ITALICS)
											.appendContent(" http://niceme.me/").withChannel(messageReceivedEvent.getMessage().getChannel())
											.build();
								} catch (RateLimitException | DiscordException | MissingPermissionsException e) {
									e.printStackTrace();
								}
							} else if (m.getContent().startsWith(".updateShard")) {
								messageReceivedEvent.getClient().setShardCount(2);
							} else if (m.getContent().startsWith(".clear")) {
								IChannel c = client.getChannelByID(m.getChannel().getID());
								if (null != c) {
									c.getMessages().stream().filter(message -> message.getAuthor().getID()
											.equalsIgnoreCase(client.getOurUser().getID())).forEach(message -> {
										try {
											Discord4J.LOGGER.debug("Attempting deletion of message {} by \"{}\" ({})", message.getID(), message.getAuthor().getName(), message.getContent());
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
								client.changePresence(!client.getOurUser().getPresence().equals(Presences.IDLE));
							} else if (m.getContent().startsWith(".game")) {
								String game = m.getContent().length() > 6 ? m.getContent().substring(6) : null;
								client.changeStatus(Status.game(game));
							} else if (m.getContent().startsWith(".type")) {
								m.getChannel().toggleTypingStatus();
							} else if (m.getContent().startsWith(".invite")) {
								try {
									m.reply("http://discord.gg/"+m.getChannel().createInvite(1800, 0, false).getInviteCode());
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
									Discord4J.LOGGER.info("{}", role.getID());
									for (Permissions permissions : role.getPermissions()) {
										permissionsJoiner.add(permissions.toString());
									}
									roleJoiner.add(role.getName()+" ("+permissionsJoiner.toString()+")");
									permissionsJoiner = new StringJoiner(", ");
								}
								try {
									Discord4J.LOGGER.info("{}", m.getAuthor().getID());
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
//								player.queue(new File("./test.mp3"));
//								player.queue(new File("./test2.mp3"));
								player.queue(new URL("https://github.com/austinv11/Discord4J/raw/master/test.mp3"));
								player.queue(new URL("https://github.com/austinv11/Discord4J/raw/master/test2.mp3"));
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
								client.getConnectedVoiceChannels().stream().filter((IVoiceChannel channel)->channel.getGuild().equals(m.getGuild())).findFirst().ifPresent(IVoiceChannel::leave);
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
								m.getChannel().getMessages().setCacheCapacity(100);
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
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					//Used for convenience in testing
					private void test(IMessage message) throws Exception {
						BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new MessageOutputStream(message.getChannel())));
						writer.write(message.getContent());
						writer.close();
					}
				});

				client.getDispatcher().registerListener(new IListener<InviteReceivedEvent>() {
					@Override
					public void handle(InviteReceivedEvent event) {
						IInvite invite = event.getInvites()[0];
						try {
							Invite.InviteResponse response = invite.details();
							event.getMessage().reply(String.format("you've invited me to join #%s in the %s guild!", response.getChannelName(), response.getGuildName()));
							invite.accept();
							client.getChannelByID(invite.details().getChannelID()).sendMessage(String.format("Hello, #%s and the \"%s\" guild! I was invited by %s!",
									response.getChannelName(), response.getGuildName(), event.getMessage().getAuthor()));
						} catch (Exception e) {
							e.printStackTrace();
						}

					}
				});

				client.getDispatcher().registerListener(new IListener<MessageDeleteEvent>() {
					@Override
					public void handle(MessageDeleteEvent event) {
						try {
							event.getMessage().reply("you said, \""+event.getMessage().getContent()+"\"");
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
