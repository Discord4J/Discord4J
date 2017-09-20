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
package discord4j.common.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import discord4j.common.jackson.DiscordEntity;

@DiscordEntity
public class AuditLogEntryEntity {

	@JsonProperty("target_id")
	private String targetId;
	private AuditLogChangeEntity[] changes;
	@JsonProperty("user_id")
	private String userId;
	private String id;
	@JsonProperty("action_type")
	private int actionType;
	private AuditLogEntryOptionsEntity options;
	private String reason;

	public String getTargetId() {
		return targetId;
	}

	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}

	public AuditLogChangeEntity[] getChanges() {
		return changes;
	}

	public void setChanges(AuditLogChangeEntity[] changes) {
		this.changes = changes;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getActionType() {
		return actionType;
	}

	public void setActionType(int actionType) {
		this.actionType = actionType;
	}

	public AuditLogEntryOptionsEntity getOptions() {
		return options;
	}

	public void setOptions(AuditLogEntryOptionsEntity options) {
		this.options = options;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}
}
