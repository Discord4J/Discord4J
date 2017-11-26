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
package discord4j.common.json.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import discord4j.common.jackson.UnsignedJson;

import javax.annotation.Nullable;
import java.util.OptionalLong;

public class AuditLogEntryOptionsResponse {

	@JsonProperty("delete_member_days")
	@Nullable
	private String deleteMemberDays;
	@JsonProperty("members_removed")
	@Nullable
	private String membersRemoved;
	@JsonProperty("channel_id")
	@UnsignedJson
	private OptionalLong channelId;
	@Nullable
	private String count;
	@UnsignedJson
	private OptionalLong id;
	@Nullable
	private String type;
	@JsonProperty("role_name")
	@Nullable
	private String roleName;

	@Nullable
	public String getDeleteMemberDays() {
		return deleteMemberDays;
	}

	@Nullable
	public String getMembersRemoved() {
		return membersRemoved;
	}

	public OptionalLong getChannelId() {
		return channelId;
	}

	@Nullable
	public String getCount() {
		return count;
	}

	public OptionalLong getId() {
		return id;
	}

	@Nullable
	public String getType() {
		return type;
	}

	@Nullable
	public String getRoleName() {
		return roleName;
	}

	@Override
	public String toString() {
		return "AuditLogEntryOptionsResponse[" +
				"deleteMemberDays=" + deleteMemberDays +
				", membersRemoved=" + membersRemoved +
				", channelId=" + channelId +
				", count=" + count +
				", id=" + id +
				", type=" + type +
				", roleName=" + roleName +
				']';
	}
}
