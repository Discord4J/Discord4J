package sx.blah.discord.handle.impl.obj;

import com.google.gson.Gson;
import org.apache.http.message.BasicNameValuePair;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.internal.DiscordEndpoints;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IInvite;
import sx.blah.discord.json.responses.InviteJSONResponse;
import sx.blah.discord.util.HTTP429Exception;
import sx.blah.discord.api.internal.Requests;

import java.util.Objects;

public class Invite implements IInvite {
	/**
	 * An invite code, AKA an invite URL minus the https://discord.gg/
	 */
	protected final String inviteCode;

	/**
	 * The human-readable version of the invite code, if available.
	 */
	protected final String xkcdPass;

	/**
	 * The client that created this object.
	 */
	protected final IDiscordClient client;

	public Invite(IDiscordClient client, String inviteCode, String xkcdPass) {
		this.client = client;
		this.inviteCode = inviteCode;
		this.xkcdPass = xkcdPass;
	}

	@Override
	public String getInviteCode() {
		return inviteCode;
	}

	@Override
	public String getXkcdPass() {
		return xkcdPass;
	}

	@Override
	public InviteResponse accept() throws DiscordException, HTTP429Exception {
		if (client.isReady()) {
			String response = Requests.POST.makeRequest(DiscordEndpoints.INVITE+inviteCode,
					new BasicNameValuePair("authorization", client.getToken()));

			InviteJSONResponse inviteResponse = new Gson().fromJson(response, InviteJSONResponse.class);

			return new InviteResponse(inviteResponse.guild.id, inviteResponse.guild.name,
					inviteResponse.channel.id, inviteResponse.channel.name);
		} else {
			Discord4J.LOGGER.error("Bot has not signed in yet!");
			return null;
		}
	}

	@Override
	public InviteResponse details() throws DiscordException, HTTP429Exception {
		if (client.isReady()) {
			String response = Requests.GET.makeRequest(DiscordEndpoints.INVITE+inviteCode,
					new BasicNameValuePair("authorization", client.getToken()));

			InviteJSONResponse inviteResponse = new Gson().fromJson(response, InviteJSONResponse.class);

			return new InviteResponse(inviteResponse.guild.id, inviteResponse.guild.name,
					inviteResponse.channel.id, inviteResponse.channel.name);
		} else {
			Discord4J.LOGGER.error("Bot has not signed in yet!");
			return null;
		}
	}

	@Override
	public void delete() throws HTTP429Exception, DiscordException {
		Requests.DELETE.makeRequest(DiscordEndpoints.INVITE+inviteCode,
				new BasicNameValuePair("authorization", client.getToken()));
	}

	@Override
	public IDiscordClient getClient() {
		return client;
	}

	@Override
	public int hashCode() {
		return Objects.hash(inviteCode);
	}

	@Override
	public boolean equals(Object other) {
		if (other == null)
			return false;

		return this.getClass().isAssignableFrom(other.getClass()) && ((IInvite) other).getInviteCode().equals(getInviteCode());
	}

	@Override
	public String toString() {
		return inviteCode;
	}
}
