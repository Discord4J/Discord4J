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
import sx.blah.discord.api.internal.DiscordUtils;
import sx.blah.discord.api.internal.json.objects.WebhookObject;
import sx.blah.discord.api.internal.json.requests.WebhookEditRequest;
import sx.blah.discord.handle.impl.events.WebhookUpdateEvent;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.Image;
import sx.blah.discord.util.PermissionUtils;

import java.util.EnumSet;
import java.util.Objects;

public class Webhook implements IWebhook {

	protected final long id;
	protected final IDiscordClient client;
	protected final IChannel channel;
	protected final IUser author;
	protected volatile String name;
	protected volatile String avatar;
	protected final String token;

	public Webhook(IDiscordClient client, String name, long id, IChannel channel, IUser author, String avatar, String token) {
		this.client = client;
		this.name = name;
		this.id = id;
		this.channel = channel;
		this.author = author;
		this.avatar = avatar;
		this.token = token;
	}

	@Override
	public long getLongID() {
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
	public String getDefaultName() {
		return name;
	}

	@Override
	public String getDefaultAvatar() {
		return avatar;
	}

	@Override
	public String getToken() {
		return token;
	}

	private void edit(String name, String avatar) {
		PermissionUtils.requirePermissions(channel, client.getOurUser(), Permissions.MANAGE_WEBHOOKS);

		WebhookObject response = ((DiscordClientImpl) client).REQUESTS.PATCH.makeRequest(
				DiscordEndpoints.WEBHOOKS + id,
				new WebhookEditRequest(name, avatar),
				WebhookObject.class);

		IWebhook oldWebhook = copy();
		IWebhook newWebhook = DiscordUtils.getWebhookFromJSON(channel, response);

		client.getDispatcher().dispatch(new WebhookUpdateEvent(oldWebhook, newWebhook));
	}

	@Override
	public void changeDefaultName(String name) {
		edit(name, null);
	}

	@Override
	public void changeDefaultAvatar(String avatar) {
		edit(this.name, avatar);
	}

	@Override
	public void changeDefaultAvatar(Image avatar) {
		edit(this.name, avatar.getData());
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
	public void delete() {
		PermissionUtils.requirePermissions(channel, client.getOurUser(), Permissions.MANAGE_WEBHOOKS);

		((DiscordClientImpl) client).REQUESTS.DELETE.makeRequest(DiscordEndpoints.WEBHOOKS + id);
	}

	@Override
	public boolean isDeleted(){
		return getChannel().getWebhookByID(id) != this;
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
		return DiscordUtils.equals(this, other);
	}
}
