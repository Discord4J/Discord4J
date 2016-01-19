package sx.blah.discord.handle.impl.obj;

import sx.blah.discord.api.DiscordException;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.HTTP403Exception;

import java.io.File;
import java.io.IOException;
import java.util.*;

//TODO
public class VoiceChannel extends Channel implements IVoiceChannel {
	
	public VoiceChannel(IDiscordClient client, String name, String id, IGuild parent, String topic, int position) {
		this(client, name, id, parent, topic, position, new ArrayList<>(), new HashMap<>(), new HashMap<>());
	}
	
	public VoiceChannel(IDiscordClient client, String name, String id, IGuild parent, String topic, int position, List<IMessage> messages, Map<String, PermissionOverride> roleOverrides, Map<String, PermissionOverride> userOverrides) {
		super(client, name, id, parent, topic, position, messages, roleOverrides, userOverrides);
	}
	
	@Override
	public List<IMessage> getMessages() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void addMessage(IMessage message) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public IMessage getMessageByID(String messageID) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public String getTopic() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void setTopic(String topic) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public String mention() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public IMessage sendMessage(String content) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public IMessage sendMessage(String content, boolean tts) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public IMessage sendFile(File file) throws HTTP403Exception, IOException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public synchronized void toggleTypingStatus() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public synchronized boolean getTypingStatus() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public String getLastReadMessageID() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public IMessage getLastReadMessage() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void edit(Optional<String> name, Optional<Integer> position, Optional<String> topic) throws DiscordException, HTTP403Exception {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void setLastReadMessageID(String lastReadMessageID) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void edit(Optional<String> name, Optional<Integer> position) throws DiscordException, HTTP403Exception {
		super.edit(name, position, Optional.empty());
	}
}
