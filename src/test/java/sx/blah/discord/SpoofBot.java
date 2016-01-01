package sx.blah.discord;

import org.apache.commons.lang3.ClassUtils;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.IListener;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.Channel;
import sx.blah.discord.handle.obj.Message;
import sx.blah.discord.util.Presences;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Used to simulate a server.
 */
public class SpoofBot {
	
	private static final Random rng = new Random();
	private Channel channel;
	private Spoofs lastSpoof;
	private Object lastSpoofData;
	private ArrayDeque<Spoofs> enqueued = new ArrayDeque<>();
	private volatile long lastTime;
	private volatile long timer;
	private final IDiscordClient client;
	
	public SpoofBot(String email, String password, String invite) throws Exception {
		client = new ClientBuilder().withLogin(email, password).login();
		client.getDispatcher().registerListener(new IListener<ReadyEvent>(){
			
			@Override
			public void handle(ReadyEvent event) {
				try {
					channel = client.getChannelByID(client.getInviteForCode(invite.replace("https://discord.gg/", "")).accept().getChannelID());
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException("Spoofing failed!");
				}
				timer = 1;
				lastTime = System.currentTimeMillis()-timer;
				new Thread(()->{
					while (true) {
						if (lastTime <= System.currentTimeMillis()-timer) {
							timer = getRandTimer();
							lastTime = System.currentTimeMillis();
							synchronized (client) {
								if (!enqueued.isEmpty()) {
									Spoofs toSpoof = enqueued.pop();
									if (toSpoof.getDependent() == null || toSpoof.getDependent().equals(lastSpoof)) {
										switch (toSpoof) {
											case MESSAGE:
												channel.toggleTypingStatus();
												lastSpoofData = channel.sendMessage(getRandMessage());
												break;
											
											case MESSAGE_EDIT:
												((Message) lastSpoofData).edit(getRandString());
												break;
											
											case TYPING_TOGGLE:
												channel.toggleTypingStatus();
												break;
											
											case GAME:
												client.updatePresence(client.getOurUser().getPresence() == Presences.IDLE,
														Optional.ofNullable(rng.nextBoolean() ? getRandString() : null));
												break;
											
											case PRESENCE:
												client.updatePresence(rng.nextBoolean(), client.getOurUser().getGame());
												break;
										}
										lastSpoof = toSpoof;
									} else {
										enqueued.addFirst(toSpoof);
										enqueued.addFirst(toSpoof.getDependent());
									}
								} else {
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
	
	public static String getRandMessage() {
		int sentenceCount = rng.nextInt(3)+1;
		String[] sentences = new String[sentenceCount];
		for (int i = 0; i < sentences.length; i++) {
			sentences[i] = getRandSentence();
		}
		
		String message = "";
		for (String sentence : sentences) {
			message += getRandEndingPunctuation()+" ";
		}
		return message;
	}
	
	public static String getRandSentence() {
		int wordCount = rng.nextInt(10)+1;
		StringJoiner joiner = new StringJoiner(" ");
		for (int i = 0; i < wordCount; i++)
			joiner.add(rng.nextInt(9) == 8 ? String.valueOf(rng.nextInt(100)) : getRandString());
		
		String message = joiner.toString();
		if (!isNumber(String.valueOf(message.charAt(0)))) {
			message = Character.toUpperCase(message.charAt(0)) + message.substring(1);
		}
		return message;
	}
	
	public static boolean isNumber(String s) {
		try {
			Integer.valueOf(s);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public static Character getRandEndingPunctuation() {
		return getRandCharacter("?!....".toCharArray()); //Weighted towards periods
	}
	
	public static long getRandTimer() {
		return (long) rng.nextDouble() * 5000L;
	}
	
	public static Number getRandNumber(Class<? extends Number> clazz) {
		Long bound;
		try {
			Field max_value = clazz.getDeclaredField("MAX_VALUE");
			bound = (Long) max_value.get(null);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			bound = (long)  Byte.MAX_VALUE;
		}
		return rng.nextDouble() * bound;
	}
	
	public static Boolean getRandBoolean() {
		return rng.nextBoolean();
	}
	
	public static Character getRandCharacter(char[] charSet) {
		return charSet[rng.nextInt(charSet.length)];
	}
	
	public static Character getRandCharacter() {
		return getRandCharacter("abcdefghijklmnopqrstuvwxyz".toCharArray());
	}
	
	public static String getRandString() {
		char[] characters = new char[rng.nextInt(64)+1];
		for (int i = 0; i < characters.length; i++) {
			characters[i] = getRandCharacter();
		}
		return new String(characters);
	}
	
	public static boolean isAllowed(Class clazz) {
		return ClassUtils.isPrimitiveOrWrapper(clazz) || clazz.equals(String.class) || clazz.equals(IDiscordClient.class);
	}
	
	public static <T> T randomizeObject(IDiscordClient client, Class<T> clazz) throws IllegalAccessException, InvocationTargetException, InstantiationException {
		if (isAllowed(clazz)) {
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
			outer:for (Constructor constructor : clazz.getConstructors()) {
				Object[] parameters = new Object[constructor.getParameterCount()];
				for (Class<?> param : constructor.getParameterTypes()) {
					if (!isAllowed(param))
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
	
	public void spoof(Spoofs toSpoof) {
		enqueued.addLast(toSpoof);
	}
	
	public enum Spoofs {
		MESSAGE("TYPING_TOGGLE"), MESSAGE_EDIT("MESSAGE"), TYPING_TOGGLE(null), GAME(null), PRESENCE(null); 
		
		String dependsOn;
		
		Spoofs(String dependsOn) {
			this.dependsOn = dependsOn;
		}
		
		public Spoofs getDependent() {
			return dependsOn == null ? null : Spoofs.valueOf(dependsOn);
		}
	}
}
