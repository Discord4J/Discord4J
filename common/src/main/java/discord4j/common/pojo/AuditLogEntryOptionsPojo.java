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
public class AuditLogEntryOptionsPojo {

	@JsonProperty("delete_member_days")
	private String deleteMemberDays;
	@JsonProperty("members_removed")
	private String membersRemoved;
	@JsonProperty("channel_id")
	private String channelId;
	private String count;
	private String id;
	private String type;
	@JsonProperty("role_name")
	private String roleName;

	public String getDeleteMemberDays() {
		return deleteMemberDays;
	}

	public void setDeleteMemberDays(String deleteMemberDays) {
		this.deleteMemberDays = deleteMemberDays;
	}

	public String getMembersRemoved() {
		return membersRemoved;
	}

	public void setMembersRemoved(String membersRemoved) {
		this.membersRemoved = membersRemoved;
	}

	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public String getCount() {
		return count;
	}

	public void setCount(String count) {
		this.count = count;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}
}
