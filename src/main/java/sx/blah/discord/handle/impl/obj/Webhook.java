package sx.blah.discord.handle.impl.obj;

import org.apache.http.entity.StringEntity;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.IShard;
import sx.blah.discord.api.internal.DiscordClientImpl;
import sx.blah.discord.api.internal.DiscordEndpoints;
import sx.blah.discord.api.internal.DiscordUtils;
import sx.blah.discord.api.internal.json.objects.WebhookObject;
import sx.blah.discord.api.internal.json.requests.WebhookEditRequest;
import sx.blah.discord.handle.impl.events.WebhookUpdateEvent;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.*;

import java.io.UnsupportedEncodingException;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;

public class Webhook implements IWebhook {

	protected final String id;
	protected final IDiscordClient client;
	protected final IChannel channel;
	protected final IUser author;
	protected volatile String name;
	protected volatile String avatar;
	protected final String token;

	public Webhook(IDiscordClient client, String name, String id, IChannel channel, IUser author, String avatar, String token) {
		this.client = client;
		this.name = name;
		this.id = id;
		this.channel = channel;
		this.author = author;
		this.avatar = avatar;
		this.token = token;
	}

	@Override
	public String getID() {
		return id;
	}

	@Override
	public IDiscordClient getClient() {
		return client;
	}

	@Override
	public IShard getShard() {
		return channel.getShard();
	}

	@Override
	public IWebhook copy() {
		return new Webhook(client, name, id, channel, author, avatar, token);
	}

	@Override
	public IGuild getGuild() {
		return channel.getGuild();
	}

	@Override
	public IChannel getChannel() {
		return channel;
	}

	@Override
	public IUser getAuthor() {
		return author;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getAvatar() {
		return avatar;
	}

	@Override
	public String getToken() {
		return token;
	}

	private void edit(Optional<String> name, Optional<String> avatar) throws MissingPermissionsException, RateLimitException, DiscordException {
		DiscordUtils.checkPermissions(client, channel, EnumSet.of(Permissions.MANAGE_WEBHOOKS));

		try {
			WebhookObject response = DiscordUtils.GSON.fromJson(((DiscordClientImpl) client).REQUESTS.PATCH.makeRequest(DiscordEndpoints.WEBHOOKS + id,
					new StringEntity(DiscordUtils.GSON.toJson(new WebhookEditRequest(name.orElse(this.name),
							avatar == null ? this.avatar : (avatar.isPresent() ? avatar.get() : null))))),
					WebhookObject.class);

			IWebhook oldWebhook = copy();
			IWebhook newWebhook = DiscordUtils.getWebhookFromJSON(channel, response);

			client.getDispatcher().dispatch(new WebhookUpdateEvent(oldWebhook, newWebhook, oldWebhook.getChannel()));
		} catch (UnsupportedEncodingException e) {
			Discord4J.LOGGER.error(LogMarkers.HANDLE, "Discord4J Internal Exception", e);
		}
	}

	@Override
	public void changeName(String name) throws RateLimitException, DiscordException, MissingPermissionsException {
		edit(Optional.of(name), Optional.empty());
	}

	@Override
	public void changeAvatar(String avatar) throws RateLimitException, DiscordException, MissingPermissionsException {
		edit(Optional.empty(), Optional.of(avatar));
	}

	@Override
	public void changeAvatar(Image avatar) throws RateLimitException, DiscordException, MissingPermissionsException {
		edit(Optional.empty(), Optional.of(avatar.getData()));
	}

	/**
	 * Sets the CACHED name of the webhook.
	 *
	 * @param name The new cached name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets the CACHED avatar of the webhook.
	 *
	 * @param avatar The new cached avatar
	 */
	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	@Override
	public void delete() throws MissingPermissionsException, RateLimitException, DiscordException {
		DiscordUtils.checkPermissions(client, channel, EnumSet.of(Permissions.MANAGE_WEBHOOKS));

		((DiscordClientImpl) client).REQUESTS.DELETE.makeRequest(DiscordEndpoints.WEBHOOKS + id);
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object other) {
		if (other == null)
			return false;

		return this.getClass().isAssignableFrom(other.getClass()) && ((IWebhook) other).getID().equals(getID());
	}
}
