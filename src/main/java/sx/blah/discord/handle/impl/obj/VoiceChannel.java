package sx.blah.discord.handle.impl.obj;

import sx.blah.discord.Discord4J;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.api.internal.DiscordClientImpl;
import sx.blah.discord.api.internal.DiscordUtils;
import sx.blah.discord.handle.impl.events.VoiceDisconnectedEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.json.requests.VoiceChannelRequest;
import sx.blah.discord.util.HTTP429Exception;
import sx.blah.discord.util.MessageList;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class VoiceChannel extends Channel implements IVoiceChannel {

	public VoiceChannel(IDiscordClient client, String name, String id, IGuild parent, String topic, int position) {
		this(client, name, id, parent, topic, position, new ArrayList<>(), new HashMap<>(), new HashMap<>());
	}

	public VoiceChannel(IDiscordClient client, String name, String id, IGuild parent, String topic, int position, List<IMessage> messages, Map<String, PermissionOverride> roleOverrides, Map<String, PermissionOverride> userOverrides) {
		super(client, name, id, parent, topic, position, roleOverrides, userOverrides);
	}

	@Override
	public void join() {
		if (client.isReady()) {
			((DiscordClientImpl) client).connectedVoiceChannel = this;
			((DiscordClientImpl) client).ws.send(DiscordUtils.GSON.toJson(new VoiceChannelRequest(parent.getID(), id, false, false)));
		} else {
			Discord4J.LOGGER.error("Bot has not signed in yet!");
		}
	}

	@Override
	public void leave(){
		if(((DiscordClientImpl) client).voiceWS != null && ((DiscordClientImpl) client).voiceWS.isConnected.get()
				&& client.getConnectedVoiceChannel().isPresent() && client.getConnectedVoiceChannel().get().equals(this)) {
			((DiscordClientImpl) client).connectedVoiceChannel = null;
			((DiscordClientImpl) client).ws.send(DiscordUtils.GSON.toJson(new VoiceChannelRequest(parent.getID(), null, false, false)));
			((DiscordClientImpl) client).voiceWS.disconnect(VoiceDisconnectedEvent.Reason.LEFT_CHANNEL);
		}
	}

	@Override
	public MessageList getMessages() {
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
	public IMessage sendMessage(String content) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IMessage sendMessage(String content, boolean tts) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IMessage sendFile(File file) throws IOException {
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
	public void changeTopic(String topic) throws HTTP429Exception, DiscordException, MissingPermissionsException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setLastReadMessageID(String lastReadMessageID) {
		throw new UnsupportedOperationException();
	}
}
