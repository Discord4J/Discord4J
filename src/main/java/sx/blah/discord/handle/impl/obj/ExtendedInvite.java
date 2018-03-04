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

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.internal.DiscordUtils;
import sx.blah.discord.api.internal.json.objects.ExtendedInviteObject;
import sx.blah.discord.handle.obj.IExtendedInvite;

import java.time.Instant;

/**
 * The default implementation of {@link IExtendedInvite}.
 */
public class ExtendedInvite extends Invite implements IExtendedInvite {

	/**
	 * The backing JSON object which holds all of the information about the invite.
	 */
	private final ExtendedInviteObject backing;

	public ExtendedInvite(IDiscordClient client, ExtendedInviteObject backing) {
		super(client, backing);
		this.backing = backing;
	}

	@Override
	public int getUses() {
		return backing.uses;
	}

	@Override
	public int getMaxUses() {
		return backing.max_uses;
	}

	@Override
	public int getMaxAge() {
		return backing.max_age;
	}

	@Override
	public boolean isTemporary() {
		return backing.temporary;
	}

	@Override
	public Instant getCreationTime() {
		return DiscordUtils.convertFromTimestamp(backing.created_at);
	}

	@Override
	public boolean isRevoked() {
		return backing.revoked;
	}
}
