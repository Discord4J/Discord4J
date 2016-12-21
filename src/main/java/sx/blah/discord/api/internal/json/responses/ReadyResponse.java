package sx.blah.discord.api.internal.json.responses;

import sx.blah.discord.api.internal.json.objects.PrivateChannelObject;
import sx.blah.discord.api.internal.json.objects.UnavailableGuildObject;
import sx.blah.discord.api.internal.json.objects.UserObject;

public class ReadyResponse {
	public String v;
	public UserObject user;
	public int[] shard;
	public String session_id;
	public PrivateChannelObject[] private_channels;
	public UnavailableGuildObject[] guilds;
	public String[] _trace;
}
