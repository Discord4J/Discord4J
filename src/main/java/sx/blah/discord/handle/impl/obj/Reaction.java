package sx.blah.discord.handle.impl.obj;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.IShard;
import sx.blah.discord.handle.obj.IEmoji;
import sx.blah.discord.handle.obj.IReaction;
import sx.blah.discord.handle.obj.IUser;

import java.util.List;

public class Reaction implements IReaction {

	protected final IShard shard;
	protected final IEmoji customEmoji;
	protected final String normalEmoji;

	/**
	 * How many users reacted
	 */
	protected volatile int count;
	/**
	 * The users that reacted
	 */
	protected volatile List<IUser> users;

	public Reaction(IShard shard, int count, List<IUser> users, IEmoji customEmoji) {
		this.shard = shard;
		this.count = count;
		this.users = users;
		this.customEmoji = customEmoji;
		this.normalEmoji = null;
	}

	public Reaction(IShard shard, int count, List<IUser> users, String normalEmoji) {
		this.shard = shard;
		this.count = count;
		this.users = users;
		this.normalEmoji = normalEmoji;
		this.customEmoji = null;
	}

	public void setCount(int count) {
		this.count = count;
	}

	@Override
	public String toString() {
		return isCustomEmoji() ? getCustomEmoji().toString() : normalEmoji;
	}

	@Override
	public boolean isCustomEmoji() {
		return customEmoji != null;
	}

	@Override
	public IEmoji getCustomEmoji() {
		return customEmoji;
	}

	@Override
	public int getCount() {
		return count;
	}

	/**
	 * This will attempt to retrieve the users if the list count doesn't match the actual count
	 *
	 * @return User list
	 */
	@Override
	public List<IUser> getUsers() {
		if (users.size() != count) {

		}

		return users;
	}

	@Override
	public IDiscordClient getClient() {
		return shard.getClient();
	}

	@Override
	public IShard getShard() {
		return shard;
	}

	@Override
	public IReaction copy() {
		return isCustomEmoji()
				? new Reaction(shard, count, users, customEmoji)
				: new Reaction(shard, count, users, normalEmoji);
	}
}
