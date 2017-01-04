package sx.blah.discord.handle.impl.events.guild.voice;

import sx.blah.discord.handle.impl.events.guild.GuildEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IVoiceChannel;

/**
 * This represents a generic voice channel event.
 */
public abstract class VoiceChannelEvent extends GuildEvent {
	
	private final IVoiceChannel voiceChannel;
	
	public VoiceChannelEvent(IVoiceChannel voiceChannel) {
		this(voiceChannel.getGuild(), voiceChannel);
	}
	
	public VoiceChannelEvent(IGuild guild, IVoiceChannel voiceChannel) {
		super(guild);
		this.voiceChannel = voiceChannel;
	}
	
	/**
	 * This gets the voice channel involved in this event.
	 *
	 * @return The voice channel.
	 */
	public IVoiceChannel getVoiceChannel() {
		return voiceChannel;
	}
}
