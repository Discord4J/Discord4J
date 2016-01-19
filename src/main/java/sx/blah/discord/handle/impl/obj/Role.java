package sx.blah.discord.handle.impl.obj;

import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import sx.blah.discord.api.DiscordEndpoints;
import sx.blah.discord.api.internal.DiscordUtils;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.json.generic.RoleResponse;
import sx.blah.discord.json.requests.RoleEditRequest;
import sx.blah.discord.util.HTTP403Exception;
import sx.blah.discord.util.Requests;

import java.awt.*;
import java.io.UnsupportedEncodingException;
import java.util.EnumSet;
import java.util.Optional;

public class Role implements IRole {
	
	/**
	 * Where the role should be displayed. -1 is @everyone, it is always last
	 */
	protected int position;
	
	/**
	 * The permissions the role has.
	 */
	protected EnumSet<Permissions> permissions;
	
	/**
	 * The role name
	 */
	protected String name;
	
	/**
	 * Whether this role is managed via plugins like twitch
	 */
	protected boolean managed;
	
	/**
	 * The role id
	 */
	protected String id;
	
	/**
	 * Whether to display this role separately from others
	 */
	protected boolean hoist;
	
	/**
	 * The DECIMAL format for the color
	 */
	protected Color color;
	
	/**
	 * The guild this role belongs to
	 */
	protected IGuild guild;
	
	public Role(int position, int permissions, String name, boolean managed, String id, boolean hoist, int color, IGuild guild) {
		this.position = position;
		this.permissions = Permissions.getAllPermissionsForNumber(permissions);
		this.name = name;
		this.managed = managed;
		this.id = id;
		this.hoist = hoist;
		this.color = new Color(color);
		this.guild = guild;
	}
	
	@Override
	public int getPosition() {
		return position;
	}
	
	/**
	 * Sets the CACHED role position.
	 *
	 * @param position The role position.
	 */
	public void setPosition(int position) {
		this.position = position;
	}
	
	@Override
	public EnumSet<Permissions> getPermissions() {
		return permissions.clone();
	}
	
	/**
	 * Sets the CACHED enabled permissions.
	 *
	 * @param permissions The permissions number.
	 */
	public void setPermissions(int permissions) {
		this.permissions = Permissions.getAllPermissionsForNumber(permissions);
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the CACHED role name.
	 *
	 * @param name The name.
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public boolean isManaged() {
		return managed;
	}
	
	@Override
	public String getID() {
		return id;
	}
	
	@Override
	public boolean isHoisted() {
		return hoist;
	}
	
	/**
	 * Sets whether this role is hoisted in the CACHE.
	 *
	 * @param hoist True if hoisted, false if otherwise.
	 */
	public void setHoist(boolean hoist) {
		this.hoist = hoist;
	}
	
	@Override
	public Color getColor() {
		return color;
	}
	
	/**
	 * Sets the CACHED role color.
	 *
	 * @param color The color decimal number.
	 */
	public void setColor(int color) {
		this.color = new Color(color);
	}
	
	@Override
	public IGuild getGuild() {
		return guild;
	}
	
	@Override
	public void edit(Optional<Color> color, Optional<Boolean> hoist, Optional<String> name, Optional<EnumSet<Permissions>> permissions) throws HTTP403Exception {
		try {
			RoleResponse response = DiscordUtils.GSON.fromJson(Requests.PATCH.makeRequest(
					DiscordEndpoints.SERVERS + guild.getID() + "/roles/" + id,
					new StringEntity(DiscordUtils.GSON.toJson(new RoleEditRequest(color.orElse(getColor()), 
							hoist.orElse(isHoisted()), name.orElse(getName()), permissions.orElse(getPermissions())))),
					new BasicNameValuePair("authorization", ((Guild) guild).client.getToken()),
					new BasicNameValuePair("content-type", "application/json")), RoleResponse.class);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void delete() throws HTTP403Exception {
		Requests.DELETE.makeRequest(DiscordEndpoints.SERVERS + guild.getID() + "/roles/" + id, 
				new BasicNameValuePair("authorization", ((Guild) guild).client.getToken()));
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	@Override
	public boolean equals(Object other) {
		return this.getClass().isAssignableFrom(other.getClass()) && ((IRole) other).getID().equals(getID());
	}
}
