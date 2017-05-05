package sx.blah.discord.handle.impl.obj;

import sx.blah.discord.handle.obj.IBanReason;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

public class BanReason implements IBanReason {

	private final IGuild guild;
	private final IUser user;
	private final String reason;

	public BanReason(IGuild guild, IUser user, String reason) {
		this.guild = guild;
		this.user = user;
		this.reason = reason;
	}

	@Override
	public String getReason() {
		return reason;
	}

	@Override
	public IUser getUser() {
		return user;
	}

	@Override
	public IGuild getGuild() {
		return guild;
	}
}

