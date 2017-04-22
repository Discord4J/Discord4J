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

package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.impl.events.guild.voice.user.UserSpeakingEvent;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;

/**
 * This is dispatched when a user starts or stops speaking
 * @deprecated Use {@link UserSpeakingEvent} instead.
 */
@Deprecated
public class VoiceUserSpeakingEvent extends UserSpeakingEvent {
	
	public VoiceUserSpeakingEvent(IVoiceChannel channel, IUser user, int ssrc, boolean speaking) {
		super(channel, user, ssrc, speaking);
	}
}
