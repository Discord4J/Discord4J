package sx.blah.discord.util;

import sx.blah.discord.Discord4J;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.api.internal.DiscordClientImpl;
import sx.blah.discord.api.internal.DiscordEndpoints;
import sx.blah.discord.api.internal.DiscordUtils;
import sx.blah.discord.api.internal.json.objects.WebhookObject;
import sx.blah.discord.handle.impl.events.*;
import sx.blah.discord.handle.obj.*;

import java.util.AbstractList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * This class is a custom implementation of {@link List} for retrieving discord webhooks.
 * <p>
 * The list gets a webhook on demand, it either fetches it from the cache or it requests the webhook from Discord
 * if not cached.
 */
public class WebhookList extends AbstractList<IWebhook> implements List<IWebhook> {

	/**
	 * This is used to cache webhook objects to prevent unnecessary queries.
	 */
	private final ConcurrentLinkedDeque<IWebhook> webhookCache = new ConcurrentLinkedDeque<>();

	/**
	 * The client that this list is respecting.
	 */
	private final DiscordClientImpl client;

	/**
	 * The channel the webhooks are from.
	 */
	private final IChannel channel;

	/**
	 * The event listener for this list instance. This is used to update the list when webhooks are received/removed/etc.
	 */
	private final WebhookListEventListener listener;

	/**
	 * This determines whether to auto load webhooks when permissions change.
	 */
	private static volatile boolean loadUponChange = true;

	/**
	 * This is true if the client object has permission to read this channel's webhooks.
	 */
	private volatile boolean hasPermission;

	/**
	 * @param client  The client for this list to respect.
	 * @param channel The channel to retrieve webhooks from.
	 */
	public WebhookList(IDiscordClient client, IChannel channel) {
		if (channel instanceof IVoiceChannel)
			throw new UnsupportedOperationException();

		this.client = (DiscordClientImpl) client;
		this.channel = channel;

		RequestBuffer.request(this::load);

		updatePermissions();

		listener = new WebhookListEventListener(this);
		client.getDispatcher().registerListener(listener);
	}

	/**
	 * This implementation of {@link List#get(int)} first checks if the requested webhook is cached, if so it retrieves
	 * that object, otherwise it requests webhooks from Discord.
	 * If the object cannot be found, it throws an {@link ArrayIndexOutOfBoundsException}.
	 *
	 * @param index The index (starting at 0) of the webhook in this list.
	 * @return The webhook object for this index.
	 */
	@Override
	public synchronized IWebhook get(int index) {
		while (size() <= index) {
			try {
				if (!load())
					throw new ArrayIndexOutOfBoundsException();
			} catch (Exception e) {
				throw new ArrayIndexOutOfBoundsException("Error querying for additional webhooks. (Cause: " + e.getClass().getSimpleName() + ")");
			}
		}

		return (IWebhook) webhookCache.toArray()[index];
	}

	/**
	 * This retrieves a webhook object with the specified webhook id.
	 *
	 * @param id The webhook id to search for.
	 * @return The webhoook object found, or null if nonexistent.
	 */
	public IWebhook get(String id) {
		IWebhook webhook = stream().filter((m) -> m.getID().equalsIgnoreCase(id)).findFirst().orElse(null);

		if (webhook == null && hasPermission && client.isReady())
			try {
				return DiscordUtils.getWebhookFromJSON(channel,
						DiscordUtils.GSON.fromJson(client.REQUESTS.GET.makeRequest(
								DiscordEndpoints.WEBHOOKS + id), WebhookObject.class));
			} catch (Exception ignored) {
			}

		return webhook;
	}

	public boolean load() throws RateLimitException {
		try {
			return queryWebhooks();
		} catch (DiscordException e) {
			Discord4J.LOGGER.error(LogMarkers.UTIL, "Discord4J Internal Exception", e);
		}
		return false;
	}

	private boolean queryWebhooks() throws DiscordException, RateLimitException {
		if (!hasPermission)
			return false;

		String response = client.REQUESTS.GET.makeRequest(DiscordEndpoints.CHANNELS + channel.getID() + "/webhooks");

		if (response == null)
			return false;

		WebhookObject[] webhooks = DiscordUtils.GSON.fromJson(response, WebhookObject[].class);

		if (webhooks.length == 0)
			return false;

		for (WebhookObject webhookResponse : webhooks) {
			add(DiscordUtils.getWebhookFromJSON(channel, webhookResponse));
		}

		return true;
	}

	@Override
	public int size() {
		return webhookCache.size();
	}

	/**
	 * This adds a webhook object to the internal webhook cache.
	 *
	 * @param webhook The webhook to cache.
	 * @return True if the object was successfully cached, false if otherwise.
	 */
	@Override
	public synchronized boolean add(IWebhook webhook) {
		return !webhookCache.contains(webhook) && webhookCache.add(webhook);

	}

	/**
	 * This removes a webhook object to the internal webhook cache.
	 *
	 * @param webhook The webhook to remove from the cache.
	 * @return True if the object was successfully removed from the cached, false if otherwise.
	 */
	public synchronized boolean remove(IWebhook webhook) {
		return webhookCache.contains(webhook) && webhookCache.remove(webhook);

	}

	public boolean contains(String id) {
		return webhookCache.stream().filter(it -> it.getID().equals(id)).findFirst().isPresent();
	}

	private void updatePermissions() {
		try {
			DiscordUtils.checkPermissions(client, channel, EnumSet.of(Permissions.MANAGE_WEBHOOKS));
			hasPermission = true;
		} catch (MissingPermissionsException e) {
			if (!Discord4J.ignoreWebhookWarnings.get())
				Discord4J.LOGGER.warn(LogMarkers.UTIL, "Missing permissions required to manage webhooks for channel {}. If this is an error, report this it the Discord4J dev!", channel.getName());
			hasPermission = false;
		}
	}

	public static class WebhookListEventListener {
		private volatile WebhookList list;

		public WebhookListEventListener(WebhookList list) {
			this.list = list;
		}

		@EventSubscriber
		public void onGuildCreate(GuildCreateEvent event) {
			if (list.channel.getGuild().equals(event.getGuild()))
				RequestBuffer.request(list::load);
		}

		@EventSubscriber
		public void onWebhookCreateEvent(WebhookCreateEvent event) {
			if (event.getWebhook().getChannel().equals(list.channel))
				list.add(event.getWebhook());
		}

		@EventSubscriber
		public void onWebhookDeleteEvent(WebhookDeleteEvent event) {
			if (event.getWebhook().getChannel().equals(list.channel)) {
				list.remove(event.getWebhook());
			}
		}

		@EventSubscriber
		public void onChannelDelete(ChannelDeleteEvent event) {
			if (event.getChannel().equals(list.channel))
				list.client.getDispatcher().unregisterListener(this);
		}

		@EventSubscriber
		public void onGuildRemove(GuildLeaveEvent event) {
			if (!(list.channel instanceof IPrivateChannel) && event.getGuild().equals(list.channel.getGuild()))
				list.client.getDispatcher().unregisterListener(this);
		}

		//The following are to update the hasPermission boolean

		@EventSubscriber
		public void onRoleUpdate(RoleUpdateEvent event) {
			if (!(list.channel instanceof IPrivateChannel) && event.getGuild().equals(list.channel.getGuild()) &&
					list.client.getOurUser().getRolesForGuild(list.channel.getGuild()).contains(event.getNewRole()))
				list.updatePermissions();
		}

		@EventSubscriber
		public void onGuildUpdate(GuildUpdateEvent event) {
			if (!(list.channel instanceof IPrivateChannel) && event.getNewGuild().equals(list.channel.getGuild()))
				list.updatePermissions();
		}

		@EventSubscriber
		public void onUserRoleUpdate(UserRoleUpdateEvent event) {
			if (!(list.channel instanceof IPrivateChannel) && event.getUser().equals(list.client.getOurUser()) && event.getGuild().equals(list.channel.getGuild()))
				list.updatePermissions();
		}

		@EventSubscriber
		public void onGuildTransferOwnership(GuildTransferOwnershipEvent event) {
			if (!(list.channel instanceof IPrivateChannel) && event.getGuild().equals(list.channel.getGuild()))
				list.updatePermissions();
		}

		@EventSubscriber
		public void onChannelUpdateEvent(ChannelUpdateEvent event) {
			if (event.getNewChannel().equals(list.channel))
				list.updatePermissions();
		}
	}
}
