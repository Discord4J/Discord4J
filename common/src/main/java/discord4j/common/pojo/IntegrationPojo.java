/*
 * This file is part of Discord4J.
 *
 * Discord4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Discord4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.common.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents an Integration Object as defined by Discord.
 *
 * @see <a href="https://discordapp.com/developers/docs/resources/guild#integration-object">Integration Object</a>
 */
public class IntegrationPojo {

	private String id;
	private String name;
	private String type;
	private boolean enabled;
	private boolean syncing;
	@JsonProperty("role_id")
	private String roleId;
	@JsonProperty("expire_behavior")
	private int expireBehavior;
	@JsonProperty("expire_grace_period")
	private int expireGracePeriod;
	private UserPojo user;
	private AccountPojo account;
	@JsonProperty("synced_at")
	private String syncedAt;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isSyncing() {
		return syncing;
	}

	public void setSyncing(boolean syncing) {
		this.syncing = syncing;
	}

	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

	public int getExpireBehavior() {
		return expireBehavior;
	}

	public void setExpireBehavior(int expireBehavior) {
		this.expireBehavior = expireBehavior;
	}

	public int getExpireGracePeriod() {
		return expireGracePeriod;
	}

	public void setExpireGracePeriod(int expireGracePeriod) {
		this.expireGracePeriod = expireGracePeriod;
	}

	public UserPojo getUser() {
		return user;
	}

	public void setUser(UserPojo user) {
		this.user = user;
	}

	public AccountPojo getAccount() {
		return account;
	}

	public void setAccount(AccountPojo account) {
		this.account = account;
	}

	public String getSyncedAt() {
		return syncedAt;
	}

	public void setSyncedAt(String syncedAt) {
		this.syncedAt = syncedAt;
	}
}
