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

import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.IShard;
import sx.blah.discord.api.internal.DiscordClientImpl;
import sx.blah.discord.api.internal.DiscordEndpoints;
import sx.blah.discord.api.internal.json.objects.ReactionUserObject;
import sx.blah.discord.handle.obj.*;

import java.util.ArrayList;
import java.util.List;

public class Reaction implements IReaction {

	private final IMessage message;
	private final int count;
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

	@Override
	public ReactionEmoji getEmoji() {
		return emoji;
	}

	@Override
	public boolean isCustomEmoji() {
		return !emoji.isUnicode();
	}

	@Override
	public IEmoji getCustomEmoji() {
		if (!isCustomEmoji()) return null;

		IEmoji emoji = getMessage().getGuild().getEmojiByID(getEmoji().getLongID());
		if (emoji == null) {
			// Make up information that we don't have. Temporary until this method is removed.
			emoji = new EmojiImpl(getEmoji().getLongID(), null, getEmoji().getName(), new ArrayList<>(), false, false);
		}
		return emoji;
	}

	@Override
	public Emoji getUnicodeEmoji() {
		return EmojiManager.getByUnicode(emoji.getName());
	}

	@Override
	public List<IUser> getUsers() {
		List<IUser> users = new ArrayList<>();

		String emoji = getEmoji().isUnicode() ? getEmoji().getName() : getEmoji().getName() + ":" + getEmoji().getStringID();
		String endpoint = String.format(DiscordEndpoints.REACTIONS_USER_LIST, getMessage().getChannel().getStringID(), getMessage().getStringID(), emoji);
		String after = "0";

		while (users.size() < count) {
			ReactionUserObject[] json = ((DiscordClientImpl) getClient()).REQUESTS.GET.makeRequest(
					endpoint + "?after=" + after + "&limit=100",
					ReactionUserObject[].class);

			for (ReactionUserObject obj : json) {
				users.add(getMessage().getChannel().getGuild().getUserByID(Long.parseUnsignedLong(obj.id)));
			}

			after = json[json.length - 1].id;
		}

		return users;
	}

	@Override
	public boolean getUserReacted(IUser user) {
		return getUsers().contains(user);
	}

	@Override
	public boolean getClientReacted() {
		return getUserReacted(getClient().getOurUser());
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
}
