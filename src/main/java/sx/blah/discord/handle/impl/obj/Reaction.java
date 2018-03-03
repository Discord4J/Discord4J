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
import sx.blah.discord.api.IShard;
import sx.blah.discord.api.internal.DiscordClientImpl;
import sx.blah.discord.api.internal.DiscordEndpoints;
import sx.blah.discord.api.internal.json.objects.UserObject;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IReaction;
import sx.blah.discord.handle.obj.IUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The default implementation of {@link IReaction}.
 */
public class Reaction implements IReaction {

	/**
	 * The message the reaction is on.
	 */
	private final IMessage message;
	/**
	 * The number of people who reacted.
	 */
	private volatile int count;
	/**
	 * The emoji of the reaction.
	 */
	private final ReactionEmoji emoji;

	public Reaction(IMessage message, int count, ReactionEmoji emoji) {
		this.message = message;
		this.count = count;
		this.emoji = emoji;
	}

	@Override
	public IMessage getMessage() {
		return message;
	}

	@Override
	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	@Override
	public ReactionEmoji getEmoji() {
		return emoji;
	}

	@Override
	public List<IUser> getUsers() {
		List<IUser> users = new ArrayList<>();

		String emoji = getEmoji().isUnicode() ? getEmoji().getName() : getEmoji().getName() + ":" + getEmoji().getStringID();
		String endpoint = String.format(DiscordEndpoints.REACTIONS_USER_LIST, getMessage().getChannel().getStringID(), getMessage().getStringID(), emoji);
		String after = "0";

		while (users.size() < count) {
			UserObject[] json = ((DiscordClientImpl) getClient()).REQUESTS.GET.makeRequest(
					endpoint + "?after=" + after + "&limit=100",
					UserObject[].class);

			for (UserObject obj : json) {
				users.add(getMessage().getShard().getUserByID(Long.parseUnsignedLong(obj.id)));
			}

			// Temporary measure so a refactor can be applied later.
			if (json.length == 0) break;

			after = json[json.length - 1].id;
		}

		return users;
	}

	@Override
	public boolean getUserReacted(IUser user) {
		return getUsers().contains(user);
	}

	@Override
	public IDiscordClient getClient() {
		return getMessage().getClient();
	}

	@Override
	public IShard getShard() {
		return getMessage().getShard();
	}

	@Override
	public IReaction copy() {
		return new Reaction(message, count, emoji);
	}

	@Override
	public boolean equals(Object other) {
		if (other == null) return false;
		if (!this.getClass().isAssignableFrom(other.getClass())) return false;

		Reaction emoji = (Reaction) other;
		return emoji.message.equals(this.message) && emoji.emoji.equals(this.emoji);
	}

	@Override
	public int hashCode() {
		return Objects.hash(message, this.emoji);
	}

	@Override
	public String toString() {
		return "Reaction(" + emoji + " : " + count + ")";
	}
}
