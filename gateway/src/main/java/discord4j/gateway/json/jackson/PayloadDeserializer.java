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
package discord4j.gateway.json.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import discord4j.discordjson.json.gateway.*;
import discord4j.gateway.json.GatewayPayload;
import discord4j.gateway.json.dispatch.EventNames;
import reactor.util.annotation.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PayloadDeserializer extends StdDeserializer<GatewayPayload<?>> {

    private static final String OP_FIELD = "op";
    private static final String D_FIELD = "d";
    private static final String T_FIELD = "t";
    private static final String S_FIELD = "s";

    private static final Map<String, Class<? extends Dispatch>> dispatchTypes = new HashMap<>();

    static {
        dispatchTypes.put(EventNames.READY, Ready.class);
        dispatchTypes.put(EventNames.RESUMED, Resumed.class);
        dispatchTypes.put(EventNames.CHANNEL_CREATE, ChannelCreate.class);
        dispatchTypes.put(EventNames.CHANNEL_UPDATE, ChannelUpdate.class);
        dispatchTypes.put(EventNames.CHANNEL_DELETE, ChannelDelete.class);
        dispatchTypes.put(EventNames.CHANNEL_PINS_UPDATE, ChannelPinsUpdate.class);
        dispatchTypes.put(EventNames.GUILD_CREATE, GuildCreate.class);
        dispatchTypes.put(EventNames.GUILD_UPDATE, GuildUpdate.class);
        dispatchTypes.put(EventNames.GUILD_DELETE, GuildDelete.class);
        dispatchTypes.put(EventNames.GUILD_BAN_ADD, GuildBanAdd.class);
        dispatchTypes.put(EventNames.GUILD_BAN_REMOVE, GuildBanRemove.class);
        dispatchTypes.put(EventNames.GUILD_EMOJIS_UPDATE, GuildEmojisUpdate.class);
        dispatchTypes.put(EventNames.GUILD_INTEGRATIONS_UPDATE, GuildIntegrationsUpdate.class);
        dispatchTypes.put(EventNames.GUILD_MEMBER_ADD, GuildMemberAdd.class);
        dispatchTypes.put(EventNames.GUILD_MEMBER_REMOVE, GuildMemberRemove.class);
        dispatchTypes.put(EventNames.GUILD_MEMBER_UPDATE, GuildMemberUpdate.class);
        dispatchTypes.put(EventNames.GUILD_MEMBERS_CHUNK, GuildMembersChunk.class);
        dispatchTypes.put(EventNames.GUILD_ROLE_CREATE, GuildRoleCreate.class);
        dispatchTypes.put(EventNames.GUILD_ROLE_UPDATE, GuildRoleUpdate.class);
        dispatchTypes.put(EventNames.GUILD_ROLE_DELETE, GuildRoleDelete.class);
        dispatchTypes.put(EventNames.GUILD_SCHEDULED_EVENT_CREATE, GuildScheduledEventCreate.class);
        dispatchTypes.put(EventNames.GUILD_SCHEDULED_EVENT_UPDATE, GuildScheduledEventUpdate.class);
        dispatchTypes.put(EventNames.GUILD_SCHEDULED_EVENT_DELETE, GuildScheduledEventDelete.class);
        dispatchTypes.put(EventNames.GUILD_SCHEDULED_EVENT_USER_ADD, GuildScheduledEventUserAdd.class);
        dispatchTypes.put(EventNames.GUILD_SCHEDULED_EVENT_USER_REMOVE, GuildScheduledEventUserRemove.class);
        dispatchTypes.put(EventNames.MESSAGE_CREATE, MessageCreate.class);
        dispatchTypes.put(EventNames.MESSAGE_UPDATE, MessageUpdate.class);
        dispatchTypes.put(EventNames.MESSAGE_DELETE, MessageDelete.class);
        dispatchTypes.put(EventNames.MESSAGE_DELETE_BULK, MessageDeleteBulk.class);
        dispatchTypes.put(EventNames.MESSAGE_REACTION_ADD, MessageReactionAdd.class);
        dispatchTypes.put(EventNames.MESSAGE_REACTION_REMOVE, MessageReactionRemove.class);
        dispatchTypes.put(EventNames.MESSAGE_REACTION_REMOVE_ALL, MessageReactionRemoveAll.class);
        dispatchTypes.put(EventNames.MESSAGE_REACTION_REMOVE_EMOJI, MessageReactionRemoveEmoji.class);
        dispatchTypes.put(EventNames.PRESENCE_UPDATE, PresenceUpdate.class);
        dispatchTypes.put(EventNames.TYPING_START, TypingStart.class);
        dispatchTypes.put(EventNames.USER_UPDATE, UserUpdate.class);
        dispatchTypes.put(EventNames.VOICE_STATE_UPDATE, VoiceStateUpdateDispatch.class);
        dispatchTypes.put(EventNames.VOICE_SERVER_UPDATE, VoiceServerUpdate.class);
        dispatchTypes.put(EventNames.WEBHOOKS_UPDATE, WebhooksUpdate.class);
        dispatchTypes.put(EventNames.INVITE_CREATE, InviteCreate.class);
        dispatchTypes.put(EventNames.INVITE_DELETE, InviteDelete.class);
        dispatchTypes.put(EventNames.APPLICATION_COMMAND_CREATE, ApplicationCommandCreate.class);
        dispatchTypes.put(EventNames.APPLICATION_COMMAND_UPDATE, ApplicationCommandUpdate.class);
        dispatchTypes.put(EventNames.APPLICATION_COMMAND_DELETE, ApplicationCommandDelete.class);
        dispatchTypes.put(EventNames.APPLICATION_COMMAND_PERMISSIONS_UPDATE, ApplicationCommandPermissionUpdate.class);
        dispatchTypes.put(EventNames.INTERACTION_CREATE, InteractionCreate.class);
        dispatchTypes.put(EventNames.THREAD_CREATE, ThreadCreate.class);
        dispatchTypes.put(EventNames.THREAD_UPDATE, ThreadUpdate.class);
        dispatchTypes.put(EventNames.THREAD_DELETE, ThreadDelete.class);
        dispatchTypes.put(EventNames.THREAD_LIST_SYNC, ThreadListSync.class);
        dispatchTypes.put(EventNames.THREAD_MEMBER_UPDATE, ThreadMemberUpdate.class);
        dispatchTypes.put(EventNames.THREAD_MEMBERS_UPDATE, ThreadMembersUpdate.class);
        dispatchTypes.put(EventNames.STAGE_INSTANCE_CREATE, StageInstanceCreate.class);
        dispatchTypes.put(EventNames.STAGE_INSTANCE_UPDATE, StageInstanceUpdate.class);
        dispatchTypes.put(EventNames.STAGE_INSTANCE_DELETE, StageInstanceDelete.class);
        dispatchTypes.put(EventNames.GUILD_AUDIT_LOG_ENTRY_CREATE, AuditLogEntryCreate.class);
        dispatchTypes.put(EventNames.AUTO_MODERATION_RULE_CREATE, AutoModRuleCreate.class);
        dispatchTypes.put(EventNames.AUTO_MODERATION_RULE_UPDATE, AutoModRuleUpdate.class);
        dispatchTypes.put(EventNames.AUTO_MODERATION_RULE_DELETE, AutoModRuleDelete.class);
        dispatchTypes.put(EventNames.AUTO_MODERATION_ACTION_EXECUTION, AutoModActionExecution.class);
        dispatchTypes.put(EventNames.INTEGRATION_CREATE, IntegrationCreate.class);
        dispatchTypes.put(EventNames.INTEGRATION_UPDATE, IntegrationUpdate.class);
        dispatchTypes.put(EventNames.INTEGRATION_DELETE, IntegrationDelete.class);
        dispatchTypes.put(EventNames.MESSAGE_POLL_VOTE_ADD, PollVoteAdd.class);
        dispatchTypes.put(EventNames.MESSAGE_POLL_VOTE_REMOVE, PollVoteRemove.class);

        // Ignored
        dispatchTypes.put(EventNames.PRESENCES_REPLACE, null);
        dispatchTypes.put(EventNames.GIFT_CODE_UPDATE, null);
        dispatchTypes.put(EventNames.GUILD_JOIN_REQUEST_DELETE, null);
        dispatchTypes.put(EventNames.GUILD_JOIN_REQUEST_UPDATE, null);
    }

    public PayloadDeserializer() {
        super(GatewayPayload.class);
    }

    @Override
    public GatewayPayload<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode payload = p.getCodec().readTree(p);

        int op = payload.get(OP_FIELD).asInt();
        String t = payload.get(T_FIELD).asText();
        Integer s = payload.get(S_FIELD).isNull() ? null : payload.get(S_FIELD).intValue();

        Class<? extends PayloadData> payloadType = getPayloadType(op, t);
        if (payloadType == GuildCreate.class) {
            JsonNode d = payload.get(D_FIELD);
            JsonNode unavailable = d.get("unavailable");
            if (unavailable != null && unavailable.asBoolean()) {
                PayloadData data = p.getCodec().treeToValue(d, UnavailableGuildCreate.class);
                return new GatewayPayload(Opcode.forRaw(op), data, s, t);
            }
        }
        PayloadData data = payloadType == null ? null : p.getCodec().treeToValue(payload.get(D_FIELD), payloadType);

        return new GatewayPayload(Opcode.forRaw(op), data, s, t);
    }

    @Nullable
    private static Class<? extends PayloadData> getPayloadType(int op, String t) {
        if (op == Opcode.DISPATCH.getRawOp()) {
            if (!dispatchTypes.containsKey(t)) {
                throw new IllegalArgumentException("Attempt to deserialize payload with unknown event type: " + t);
            }
            return dispatchTypes.get(t);
        }

        Opcode<?> opcode = Opcode.forRaw(op);
        if (opcode == null) {
            throw new IllegalArgumentException("Attempt to deserialize payload with unknown op: " + op);
        }
        return opcode.getPayloadType();
    }
}
