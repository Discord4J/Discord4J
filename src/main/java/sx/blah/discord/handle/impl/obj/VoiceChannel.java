package sx.blah.discord.handle.impl.obj;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IVoiceChannel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//TODO
public class VoiceChannel extends Channel implements IVoiceChannel {
	
	public VoiceChannel(IDiscordClient client, String name, String id, IGuild parent, String topic, int position) {
		this(client, name, id, parent, topic, position, new ArrayList<>(), new HashMap<>(), new HashMap<>());
	}
	
	public VoiceChannel(IDiscordClient client, String name, String id, IGuild parent, String topic, int position, List<IMessage> messages, Map<String, PermissionOverride> roleOverrides, Map<String, PermissionOverride> userOverrides) {
		super(client, name, id, parent, topic, position, messages, roleOverrides, userOverrides);
	}
}
