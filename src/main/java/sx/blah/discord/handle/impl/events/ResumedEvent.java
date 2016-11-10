package sx.blah.discord.handle.impl.events;

import sx.blah.discord.api.IShard;
import sx.blah.discord.api.events.Event;

public class ResumedEvent extends Event {
	private IShard shard;

	public ResumedEvent(IShard shard) {
		this.shard = shard;
	}

	public  IShard getShard() {
		return this.shard;
	}
}
