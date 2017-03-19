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

package sx.blah.discord.api.internal.json.requests;

import sx.blah.discord.handle.impl.obj.VoiceChannel;
import sx.blah.discord.handle.obj.IRole;

import java.util.Arrays;

public class MemberEditRequest {
	/**
	 * Roles for the user to have.
	 */
	public String[] roles;

	/**
	 * Changes the user's nickname.
	 */
	public String nick;

	/**
	 * Whether to mute the user.
	 */
	public Boolean mute;

	/**
	 * Whether to deafen the user.
	 */
	public Boolean deaf;

	/**
	 * The voice channel to move the user to.
	 */
	public String channel_id;

	public MemberEditRequest(IRole[] roles, String nick, boolean mute, boolean deaf, VoiceChannel channel) {
		this.roles = Arrays.stream(roles).map(IRole::getID).distinct().toArray(String[]::new);
		this.nick = nick;
		this.mute = mute;
		this.deaf = deaf;
		this.channel_id = channel.getID();
	}

	public MemberEditRequest(String[] roles) {
		this.roles = roles;
	}

	public MemberEditRequest(IRole[] roles) {
		this.roles = Arrays.stream(roles).map(IRole::getID).distinct().toArray(String[]::new);
	}

	public MemberEditRequest(String channelID) {
		this.channel_id = channelID;
	}

	public MemberEditRequest(boolean deafen) {
		this.deaf = deafen;
	}

	public MemberEditRequest(boolean mute, boolean overloadsSuck) {
		this.mute = mute;
	}

	public MemberEditRequest(String nick, boolean overloadsSuck) {
		this.nick = nick;
	}
}
