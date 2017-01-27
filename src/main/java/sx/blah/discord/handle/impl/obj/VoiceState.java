package sx.blah.discord.handle.impl.obj;

import sx.blah.discord.handle.obj.*;

public class VoiceState implements IVoiceState {

	private final IGuild guild;
	private final IVoiceChannel channel;
	private final IUser user;
	private final String sessionID;
	private boolean isDeafened;
	private boolean isMuted;
	private final boolean isSelfDeafened;
	private final boolean isSelfMuted;
	private final boolean isSuppressed;

	public VoiceState(IGuild guild, IVoiceChannel channel, IUser user, String sessionID, boolean isDeafened, boolean isMuted, boolean isSelfDeafened, boolean isSelfMuted, boolean isSuppressed) {
		this.guild = guild;
		this.channel = channel;
		this.user = user;
		this.sessionID = sessionID;
		this.isDeafened = isDeafened;
		this.isMuted = isMuted;
		this.isSelfDeafened = isSelfDeafened;
		this.isSelfMuted = isSelfMuted;
		this.isSuppressed = isSuppressed;
	}

	public VoiceState(IGuild guild, IUser user) {
		this(guild, null, user, null, false, false, false, false, false);
	}

	@Override
	public IGuild getGuild() {
		return guild;
	}

	@Override
	public IVoiceChannel getChannel() {
		return channel;
	}

	@Override
	public IUser getUser() {
		return user;
	}

	@Override
	public String getSessionID() {
		return sessionID;
	}

	@Override
	public boolean isDeafened() {
		return isDeafened;
	}

	public void setDeafened(boolean isDeafened) {
		this.isDeafened = isDeafened;
	}

	@Override
	public boolean isMuted() {
		return isMuted;
	}

	public void setMuted(boolean isMuted) {
		this.isMuted = isMuted;
	}

	@Override
	public boolean isSelfDeafened() {
		return isSelfDeafened;
	}

	@Override
	public boolean isSelfMuted() {
		return isSelfMuted;
	}

	@Override
	public boolean isSuppressed() {
		return isSuppressed;
	}
}
