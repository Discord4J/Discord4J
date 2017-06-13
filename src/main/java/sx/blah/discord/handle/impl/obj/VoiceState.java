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

package sx.blah.discord.handle.impl.obj;

import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.handle.obj.IVoiceState;

public class VoiceState implements IVoiceState {

	private final IGuild guild;
	private final IUser user;
	private final String sessionID;
	private IVoiceChannel channel;
	private boolean isDeafened;
	private boolean isMuted;
	private boolean isSelfDeafened;
	private boolean isSelfMuted;
	private final boolean isSuppressed;

	public VoiceState(IGuild guild, IVoiceChannel channel, IUser user, String sessionID, boolean isDeafened, boolean isMuted, boolean isSelfDeafened, boolean isSelfMuted, boolean isSuppressed) {
		this.guild = guild;
		this.user = user;
		this.sessionID = sessionID;
		this.channel = channel;
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

	public void setChannel(IVoiceChannel channel) {
		this.channel = channel;
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

	public void setSelfDeafened(boolean isSelfDeafened) {
		this.isSelfDeafened = isSelfDeafened;
	}

	@Override
	public boolean isSelfMuted() {
		return isSelfMuted;
	}

	public void setSelfMuted(boolean isSelfMuted) {
		this.isSelfMuted = isSelfMuted;
	}

	@Override
	public boolean isSuppressed() {
		return isSuppressed;
	}
}
