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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import discord4j.common.jackson.DiscordPojoFilter;

@JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = DiscordPojoFilter.class)
public class AuditLogPojo {

	private WebhookPojo[] webhooks;
	private UserPojo[] users;
	@JsonProperty("audit_log_entries")
	private AuditLogEntryPojo[] auditLogEntries;

	public WebhookPojo[] getWebhooks() {
		return webhooks;
	}

	public void setWebhooks(WebhookPojo[] webhooks) {
		this.webhooks = webhooks;
	}

	public UserPojo[] getUsers() {
		return users;
	}

	public void setUsers(UserPojo[] users) {
		this.users = users;
	}

	public AuditLogEntryPojo[] getAuditLogEntries() {
		return auditLogEntries;
	}

	public void setAuditLogEntries(AuditLogEntryPojo[] auditLogEntries) {
		this.auditLogEntries = auditLogEntries;
	}
}
