package sx.blah.discord.handle.impl.obj;

import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.internal.DiscordClientImpl;
import sx.blah.discord.api.internal.DiscordEndpoints;
import sx.blah.discord.api.internal.DiscordUtils;
import sx.blah.discord.api.internal.Requests;
import sx.blah.discord.handle.obj.IApplication;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.json.requests.BotConversionRequest;
import sx.blah.discord.json.responses.ApplicationResponse;
import sx.blah.discord.json.responses.BotResponse;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.HTTP429Exception;
import sx.blah.discord.util.Image;

import java.io.UnsupportedEncodingException;
import java.util.EnumSet;
import java.util.Optional;

public class Application implements IApplication {

	/**
	 * Application secret key.
	 */
	protected final String secret;

	/**
	 * The application's oauth redirect uris.
	 */
	protected volatile String[] redirectUris;

	/**
	 * The application's description.
	 */
	protected volatile String description;

	/**
	 * The application name.
	 */
	protected volatile String name;

	/**
	 * The application id.
	 */
	protected final String id;

	/**
	 * The application icon.
	 */
	protected volatile String icon;

	/**
	 * The bot tied to this application.
	 */
	protected volatile IUser bot;

	/**
	 * The auth token for the bot tied to this application.
	 */
	protected volatile String botToken;

	/**
	 * The discord client instance.
	 */
	protected final IDiscordClient client;

	public Application(IDiscordClient client, String secret, String[] redirectUris, String description, String name, String id, String icon, IUser bot, String botToken) {
		this.client = client;
		this.secret = secret;
		this.redirectUris = redirectUris;
		this.description = description;
		this.name = name;
		this.id = id;
		this.icon = icon;
		this.bot = bot;
		this.botToken = botToken;
	}

	@Override
	public String getSecret() {
		return secret;
	}

	@Override
	public String[] getRedirectUris() {
		return redirectUris.clone();
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getID() {
		return id;
	}

	@Override
	public String getIcon() {
		return icon;
	}

	@Override
	public String getIconUrl() {
		return String.format(DiscordEndpoints.APPLICATION_ICON, id, icon);
	}

	private void edit(Optional<String> name, Optional<String> description, Optional<Image> icon, Optional<String[]> redirectUris) throws DiscordException, HTTP429Exception {
		try {
			ApplicationResponse response = DiscordUtils.GSON.fromJson(Requests.PUT.makeRequest(DiscordEndpoints.APPLICATIONS+"/"+id,
					new StringEntity(DiscordUtils.GSON_NO_NULLS.toJson(new ApplicationResponse(redirectUris.orElse(this.redirectUris),
							name.orElse(this.name),
							description.orElse(this.description), icon == null ?
							this.icon : (icon.isPresent() ? icon.get().getData() : null)))),
					new BasicNameValuePair("authorization", client.getToken()),
					new BasicNameValuePair("content-type", "application/json")), ApplicationResponse.class);

			this.name = response.name;
			this.description = response.description;
			this.icon = response.icon;
			this.redirectUris = response.redirect_uris;
		} catch (UnsupportedEncodingException e) {
			Discord4J.LOGGER.error("Discord4J Internal Exception", e);
		}
	}

	@Override
	public void changeName(String name) throws HTTP429Exception, DiscordException {
		edit(Optional.of(name), Optional.empty(), null, Optional.empty());
	}

	@Override
	public void changeDescription(String description) throws HTTP429Exception, DiscordException {
		edit(Optional.empty(), Optional.of(description), null, Optional.empty());
	}

	@Override
	public void changeIcon(Optional<Image> icon) throws HTTP429Exception, DiscordException {
		edit(Optional.empty(), Optional.empty(), icon, Optional.empty());
	}

	@Override
	public void changeRedirectUris(String[] redirectUris) throws HTTP429Exception, DiscordException {
		edit(Optional.empty(), Optional.empty(), null, Optional.of(redirectUris));
	}

	@Override
	public String convertUserToBot(String token) throws DiscordException {
		try {
			System.out.println(DiscordUtils.GSON.toJson(new BotConversionRequest(token)));
			BotResponse response = DiscordUtils.GSON.fromJson(Requests.POST.makeRequest(
					DiscordEndpoints.APPLICATIONS+"/"+id+"/bot",
					new StringEntity(DiscordUtils.GSON.toJson(new BotConversionRequest(token))),
					new BasicNameValuePair("authorization", client.getToken()),
					new BasicNameValuePair("content-type", "application/json")), BotResponse.class);

			this.bot = DiscordUtils.getUserFromJSON(client, response);
			this.botToken = response.token;
			return botToken;
		} catch (HTTP429Exception | UnsupportedEncodingException e) {
			Discord4J.LOGGER.error("Discord4J Internal Exception", e);
		}
		return null;
	}

	@Override
	public String convertUserToBot(IDiscordClient client) throws DiscordException {
		if (client.isBot())
			throw new DiscordException("Client is already a bot!");
		if (client.isReady() && client.getToken().equals(this.client.getToken()))
			throw new DiscordException("Cannot convert application owner to a bot!");

		String token = convertUserToBot(client.getToken());
		((DiscordClientImpl) client).convert(token);
		return token;
	}

	@Override
	public ClientBuilder createBot() throws DiscordException {
		try {
			BotResponse response = DiscordUtils.GSON.fromJson(Requests.POST.makeRequest(
					DiscordEndpoints.APPLICATIONS+"/"+id+"/bot",
					new StringEntity("{}"), //Still needs to send a json, but it has to be empty
					new BasicNameValuePair("authorization", client.getToken()),
					new BasicNameValuePair("content-type", "application/json")), BotResponse.class);
			this.bot = DiscordUtils.getUserFromJSON(client, response);
			this.botToken = response.token;
			return new ClientBuilder().withToken(botToken);
		} catch (HTTP429Exception | UnsupportedEncodingException e) {
			Discord4J.LOGGER.error("Discord4J Internal Exception", e);
		}
		return null;
	}

	@Override
	public void delete() throws DiscordException {
		try {
			Requests.DELETE.makeRequest(DiscordEndpoints.APPLICATIONS+"/"+id,
					new BasicNameValuePair("authorization", client.getToken()));
		} catch (HTTP429Exception e) {
			Discord4J.LOGGER.error("Discord4J Internal Exception", e);
		}
	}

	@Override
	public String createBotInvite(Optional<EnumSet<Permissions>> requestedPermissions, Optional<String> guildID) {
		String url = DiscordEndpoints.AUTHORIZE+"?client_id=%s&scope=bot";

		if (requestedPermissions.isPresent())
			url += "&permissions="+Permissions.generatePermissionsNumber(requestedPermissions.get());

		if (guildID.isPresent())
			url += "&guild_id="+guildID.get();

		return String.format(url, id);
	}

	@Override
	public IUser getBotUser() {
		return bot;
	}

	@Override
	public String getBotToken() {
		return botToken;
	}

	@Override
	public IDiscordClient getClient() {
		return client;
	}

	@Override
	public IApplication copy() {
		return new Application(client, secret, redirectUris, description, name, id, icon, bot, botToken);
	}
}
