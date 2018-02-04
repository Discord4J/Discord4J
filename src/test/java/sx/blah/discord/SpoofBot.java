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

import org.apache.commons.lang3.ClassUtils;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.awt.Color;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayDeque;
import java.util.EnumSet;
import java.util.Random;

/**
 * Used to simulate a server.
 */
public class SpoofBot {

	private static final Random rng = new Random();
	private IChannel channel;
	private Spoofs lastSpoof;
	private Object lastSpoofData;
	private ArrayDeque<Spoofs> enqueued = new ArrayDeque<>();
	private volatile long lastTime;
	private volatile long timer;
	private final IDiscordClient other;
	private final IDiscordClient client;

	public SpoofBot(IDiscordClient other, String token, long channelID) throws Exception {
		this.other = other;
		client = new ClientBuilder().withToken(token).login();
		client.getDispatcher().registerListener(new IListener<ReadyEvent>() {

			@Override
			public void handle(ReadyEvent event) {
				try {
					channel = client.getChannelByID(channelID);
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException("Spoofing failed!");
				}
				timer = 1;
				lastTime = System.currentTimeMillis()-timer;
				new Thread(() -> {
					while (true) {
						if (lastTime <= System.currentTimeMillis()-timer) {
							//Time for the next spoof
							timer = getRandTimer();
							lastTime = System.currentTimeMillis();
							synchronized (client) {
								if (!enqueued.isEmpty()) {
									Spoofs toSpoof = enqueued.pop();
									if (toSpoof.getDependent() == null || toSpoof.getDependent().equals(lastSpoof)) {
										//Handle each spoof
										switch (toSpoof) {
											case MESSAGE:
												channel.toggleTypingStatus();
												try {
													lastSpoofData = channel.sendMessage((rng.nextInt(10) == 9 ?
															other.getOurUser().mention()+" " : "")+getRandMessage());
												} catch (MissingPermissionsException | RateLimitException | DiscordException e) {
													e.printStackTrace();
												}
												break;

											case MESSAGE_EDIT:
												try {
													((IMessage) lastSpoofData).edit(getRandMessage());
												} catch (MissingPermissionsException | RateLimitException | DiscordException e) {
													e.printStackTrace();
												}
												break;

											case TYPING_TOGGLE:
												channel.toggleTypingStatus();
												break;

											case GAME:
												client.changePresence(StatusType.ONLINE, ActivityType.PLAYING, rng.nextBoolean() ? getRandString() : null);
												break;

											case PRESENCE:
												break;

											case MESSAGE_DELETE:
												try {
													((IMessage) lastSpoofData).delete();
												} catch (MissingPermissionsException | RateLimitException | DiscordException e) {
													e.printStackTrace();
												}
												break;

											case INVITE:
												IInvite invite = null;
												try {
													invite = client.getGuilds().get(0).getChannels().get(
															rng.nextInt(client.getGuilds().get(0).getChannels().size()))
															.createInvite(18000, 1, false, false);
												} catch (MissingPermissionsException | RateLimitException | DiscordException e) {
													e.printStackTrace();
												}
												if (invite.getCode() != null) {
													try {
														channel.sendMessage("https://discord.gg/"+invite.getCode());
													} catch (MissingPermissionsException | RateLimitException | DiscordException e) {
														e.printStackTrace();
													}
												}
												lastSpoofData = invite;
												break;

											case CHANNEL_CREATE_AND_DELETE:
												try {
													final IChannel newChannel = channel.getGuild().createChannel(getRandString());
													final long deletionTimer = getRandTimer()+System.currentTimeMillis();
													new Thread(() -> {
														while (deletionTimer > System.currentTimeMillis()) {
														}
														try {
															newChannel.delete();
														} catch (MissingPermissionsException | RateLimitException | DiscordException e) {
															e.printStackTrace();
														}
													}).start();
												} catch (DiscordException | MissingPermissionsException | RateLimitException e) {
													e.printStackTrace();
												}
												break;

											case CHANNEL_EDIT:
												try {
													channel.changeName(getRandString());
													channel.changeTopic(getRandSentence());
												} catch (DiscordException | MissingPermissionsException | RateLimitException e) {
													e.printStackTrace();
												}
												break;

											case ROLE_CREATE_EDIT_AND_DELETE:
												try {
													final IRole role = channel.getGuild().createRole();
													role.changeColor(new Color(rng.nextInt(255), rng.nextInt(255), rng.nextInt(255)));
													role.changeName(getRandString());
													role.changePermissions(EnumSet.allOf(Permissions.class));
													final long deletionTimer = getRandTimer()+System.currentTimeMillis();
													new Thread(() -> {
														while (deletionTimer > System.currentTimeMillis()) {
														}
														try {
															role.delete();
														} catch (MissingPermissionsException | RateLimitException | DiscordException e) {
															e.printStackTrace();
														}
													}).start();
												} catch (MissingPermissionsException | RateLimitException | DiscordException e) {
													e.printStackTrace();
												}
												break;
										}
										lastSpoof = toSpoof;
									} else {
										//Dependent missing? Not a problem, we'll just do the dependent instead
										enqueued.addFirst(toSpoof);
										enqueued.addFirst(toSpoof.getDependent());
										timer = 0;
									}
								} else {
									//No spoofs queued, better randomize them
									for (int i = 0; i < 10; i++)
										enqueued.add(Spoofs.values()[rng.nextInt(EnumSet.allOf(Spoofs.class).size())]);
								}
							}
						}
					}
				}).start();
			}
		});
	}

	/**
	 * Generates a random message.
	 *
	 * @return The random message.
	 */
	public static String getRandMessage() {
		int sentenceCount = rng.nextInt(3)+1; //The message will have 1-3 sentences.
		String[] sentences = new String[sentenceCount];
		for (int i = 0; i < sentences.length; i++) {
			sentences[i] = getRandSentence();
		}

		String message = "";
		for (String sentence : sentences) {
			message += sentence+getRandEndingPunctuation()+" ";
		}
		return message;
	}

	/**
	 * Generates a random sentence.
	 *
	 * @return The random sentence.
	 */
	public static String getRandSentence() {
		int wordCount = rng.nextInt(10)+1;
		String[] words = new String[wordCount];
		for (int i = 0; i < wordCount; i++)
			words[i] = rng.nextInt(9) == 8 ? String.valueOf(rng.nextInt(100)) : getRandString(); //Gets either a random number or a random string

		String message = "";
		for (String word : words) {
			message += word+getRandCharacter("      :,;".toCharArray()); //randomizes inline punctuation, weighs spaces more heavily
			if (message.charAt(message.length()-1) != ' ')
				message += " ";
		}
		message = message.substring(0, message.length()-2);

		if (!isNumber(String.valueOf(message.charAt(0)))) {
			message = Character.toUpperCase(message.charAt(0))+message.substring(1);
		}
		return message;
	}

	/**
	 * Checks whether a string is a number.
	 *
	 * @param s The string to check.
	 * @return True if the string is a number, false if otherwise.
	 */
	public static boolean isNumber(String s) {
		try {
			Integer.valueOf(s);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Gets a random ending punctuation.
	 *
	 * @return The random character.
	 */
	public static Character getRandEndingPunctuation() {
		return getRandCharacter("?!....".toCharArray()); //Weighted towards periods
	}

	/**
	 * Randomizes the time to wait until the next spoof action.
	 *
	 * @return The time in milliseconds.
	 */
	public static long getRandTimer() {
		return 1000L+(long) (rng.nextDouble()*(3000L-1000L)); //Timer between 1 to 3 seconds
	}

	/**
	 * Gets a random number based on the number type provided.
	 *
	 * @param clazz The number type class.
	 * @return The randomized number.
	 */
	public static Number getRandNumber(Class<? extends Number> clazz) {
		Long bound;
		try {
			Field max_value = clazz.getDeclaredField("MAX_VALUE");
			bound = (Long) max_value.get(null);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			bound = (long) Byte.MAX_VALUE; //Supports all possible number types
		}
		return rng.nextDouble()*bound;
	}

	/**
	 * Gets a random boolean.
	 *
	 * @return The random boolean.
	 */
	public static Boolean getRandBoolean() {
		return rng.nextBoolean();
	}

	/**
	 * Gets a randomized character from the specificed charset.
	 *
	 * @param charSet The character set.
	 * @return The randomized character.
	 */
	public static Character getRandCharacter(char[] charSet) {
		return charSet[rng.nextInt(charSet.length)];
	}

	/**
	 * Gets a random alphabetical character.
	 *
	 * @return The random character.
	 */
	public static Character getRandCharacter() {
		return getRandCharacter("abcdefghijklmnopqrstuvwxyz".toCharArray());
	}

	/**
	 * Randomizes a string.
	 *
	 * @return The random string.
	 */
	public static String getRandString() {
		char[] characters = new char[rng.nextInt(16)+3]; //Uses 3-16 characters
		for (int i = 0; i < characters.length; i++) {
			characters[i] = getRandCharacter();
		}
		return new String(characters);
	}

	/**
	 * Checks if the class is supported to be randomized without recursion.
	 *
	 * @param clazz The class to check.
	 * @return True if supported, false if otherwise.
	 */
	public static boolean canBeRandomized(Class clazz) {
		return ClassUtils.isPrimitiveOrWrapper(clazz) || clazz.equals(String.class) || clazz.equals(IDiscordClient.class);
	}

	/**
	 * Randomly constructs an object.
	 *
	 * @param client The discord client.
	 * @param clazz The class to construct.
	 * @param <T> The type of object to construct.
	 * @return The constructed object (or null if not possible).
	 *
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws InstantiationException
	 */
	public static <T> T randomizeObject(IDiscordClient client, Class<T> clazz) throws IllegalAccessException, InvocationTargetException, InstantiationException {
		if (canBeRandomized(clazz)) {
			if (String.class.isAssignableFrom(clazz))
				return (T) getRandString();
			else if (Character.class.isAssignableFrom(clazz))
				return (T) getRandCharacter();
			else if (Boolean.class.isAssignableFrom(clazz))
				return (T) getRandBoolean();
			else if (Number.class.isAssignableFrom(clazz))
				return (T) getRandNumber((Class<? extends Number>) clazz);
			else if (Void.class.isAssignableFrom(clazz))
				return null;
			else if (IDiscordClient.class.isAssignableFrom(clazz))
				return (T) client;

		} else {
			outer:
			for (Constructor constructor : clazz.getConstructors()) {
				Object[] parameters = new Object[constructor.getParameterCount()];
				for (Class<?> param : constructor.getParameterTypes()) {
					if (!canBeRandomized(param))
						continue outer;
				}
				if (parameters.length > 0) {
					for (int i = 0; i < parameters.length; i++) {
						parameters[i] = randomizeObject(client, constructor.getParameterTypes()[i]);
					}
					return (T) constructor.newInstance(parameters);
				} else {
					return (T) constructor.newInstance();
				}
			}
		}
		return null;
	}

	/**
	 * Adds specific spoofs
	 *
	 * @param toSpoof The specific spoof to add
	 */
	public void spoof(Spoofs toSpoof) {
		enqueued.addLast(toSpoof);
	}

	/**
	 * Represents the kind of spoofing this bot is capable of.
	 */
	public enum Spoofs {
		MESSAGE("TYPING_TOGGLE"), MESSAGE_EDIT("MESSAGE"), TYPING_TOGGLE(null), GAME(null), PRESENCE(null),
		MESSAGE_DELETE("MESSAGE"), INVITE(null), CHANNEL_CREATE_AND_DELETE(null), CHANNEL_EDIT(null),
		ROLE_CREATE_EDIT_AND_DELETE(null);

		String dependsOn;

		Spoofs(String dependsOn) {
			this.dependsOn = dependsOn;
		}

		/**
		 * Gets the spoof required for this spoof to run.
		 *
		 * @return The dependent spoof.
		 */
		public Spoofs getDependent() {
			return dependsOn == null ? null : Spoofs.valueOf(dependsOn);
		}
	}
}
