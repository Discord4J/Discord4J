package sx.blah.discord.handle.impl.events;

import sx.blah.discord.api.events.Event;

/**
 * This event is fired when the api has established an initial connection to the Discord gateway.
 * At this point, the bot has <b>not</b> received all of the necessary information to interact with all aspects of the api.
 * Wait for {@link ReadyEvent} to do so.
 */
public class LoginEvent extends Event {
}
