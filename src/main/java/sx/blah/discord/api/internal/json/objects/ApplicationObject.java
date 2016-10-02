package sx.blah.discord.api.internal.json.objects;

import com.google.gson.annotations.Expose;

public class ApplicationObject {
	@Expose(serialize = false)
	public String secret;
	public String[] redirect_uris;
	public String description;
	public String name;
	public String id;
	@Expose(serialize = false)
	public String icon;
	public BotObject bot;

	public ApplicationObject(String[] redirect_uris, String name, String description, String icon) {
		this.redirect_uris = redirect_uris;
		this.description = description;
		this.name = name;
		this.icon = icon;
	}

	public static class BotObject extends UserObject {
		public String token;
	}
}
