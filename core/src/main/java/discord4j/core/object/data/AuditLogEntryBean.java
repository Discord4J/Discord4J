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
package discord4j.core.object.data;

import discord4j.core.object.audit.AuditLogChange;
import discord4j.core.util.AuditLogUtil;
import discord4j.rest.json.response.AuditLogEntryResponse;
import reactor.util.annotation.Nullable;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

public class AuditLogEntryBean implements Serializable {

    private static final long serialVersionUID = -1710562399643235117L;

    private long id;
    private long targetId;
    private long responsibleUserId;
    @Nullable
    private String reason;
    private int actionType;
    private Map<String, AuditLogChange<?>> changes;
    private Map<String, ?> options;

    public AuditLogEntryBean(final AuditLogEntryResponse response) {
        this.id = response.getId();
        this.targetId = response.getTargetId() == null ? 0 : response.getTargetId();
        this.responsibleUserId = response.getUserId();
        this.reason = response.getReason();
        this.actionType = response.getActionType();

        this.changes = response.getChanges() == null
                ? Collections.emptyMap()
                : Arrays.stream(response.getChanges()).collect(AuditLogUtil.changeCollector());

        this.options = response.getOptions() == null
                ? Collections.emptyMap()
                : AuditLogUtil.createOptionMap(response.getOptions());
    }

    public AuditLogEntryBean() {}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTargetId() {
        return targetId;
    }

    public void setTargetId(long targetId) {
        this.targetId = targetId;
    }

    public long getResponsibleUserId() {
        return responsibleUserId;
    }

    public void setResponsibleUserId(long responsibleUserId) {
        this.responsibleUserId = responsibleUserId;
    }

    @Nullable
    public String getReason() {
        return reason;
    }

    public void setReason(@Nullable String reason) {
        this.reason = reason;
    }

    public int getActionType() {
        return actionType;
    }

    public void setActionType(int actionType) {
        this.actionType = actionType;
    }

    public Map<String, AuditLogChange<?>> getChanges() {
        return changes;
    }

    public void setChanges(Map<String, AuditLogChange<?>> changes) {
        this.changes = changes;
    }

    public Map<String, ?> getOptions() {
        return options;
    }

    public void setOptions(Map<String, ?> options) {
        this.options = options;
    }

    @Override
    public String toString() {
        return "AuditLogEntryBean{" +
                "id=" + id +
                ", targetId=" + targetId +
                ", responsibleUserId=" + responsibleUserId +
                ", reason='" + reason + '\'' +
                ", actionType=" + actionType +
                ", changes=" + changes +
                ", options=" + options +
                '}';
    }
}
