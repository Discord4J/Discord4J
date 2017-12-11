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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J. If not, see <http://www.gnu.org/licenses/>.
 */

package discord4j.common.json.payload;

import discord4j.common.json.payload.dispatch.Dispatch;

public final class Opcode<T extends PayloadData> {

	public static final Opcode<Dispatch> DISPATCH = newOp(0, Dispatch.class);
	public static final Opcode<Heartbeat> HEARTBEAT = newOp(1, Heartbeat.class);
	public static final Opcode<Identify> IDENTIFY = newOp(2, Identify.class);
	public static final Opcode<StatusUpdate> STATUS_UPDATE = newOp(3, StatusUpdate.class);
	public static final Opcode<VoiceStateUpdate> VOICE_STATE_UPDATE = newOp(4, VoiceStateUpdate.class);
	public static final Opcode<Null> VOICE_SERVER_PING = newOp(5, Null.class);
	public static final Opcode<Resume> RESUME = newOp(6, Resume.class);
	public static final Opcode<Null> RECONNECT = newOp(7, Null.class);
	public static final Opcode<RequestGuildMembers> REQUEST_GUILD_MEMBERS = newOp(8, RequestGuildMembers.class);
	public static final Opcode<InvalidSession> INVALID_SESSION = newOp(9, InvalidSession.class);
	public static final Opcode<Hello> HELLO = newOp(10, Hello.class);
	public static final Opcode<Null> HEARTBEAT_ACK = newOp(11, Null.class);

	private final int rawOp;
	private final Class<T> payloadType;

	private Opcode(int rawOp, Class<T> payloadType) {
		this.rawOp = rawOp;
		this.payloadType = payloadType;
	}

	public static Opcode<?> forRaw(int rawOp) {
		switch (rawOp) {
			case 0: return DISPATCH;
			case 1: return HEARTBEAT;
			case 2: return IDENTIFY;
			case 3: return STATUS_UPDATE;
			case 4: return VOICE_STATE_UPDATE;
			case 5: return VOICE_SERVER_PING;
			case 6: return RESUME;
			case 7: return RECONNECT;
			case 8: return REQUEST_GUILD_MEMBERS;
			case 9: return INVALID_SESSION;
			case 10: return HELLO;
			case 11: return HEARTBEAT_ACK;
			default: return null;
		}
	}

	private static <T extends PayloadData> Opcode<T> newOp(int rawOp, Class<T> payloadType) {
		return new Opcode<>(rawOp, payloadType);
	}

	public int getRawOp() {
		return rawOp;
	}

	public Class<T> getPayloadType() {
		return payloadType;
	}

	@Override
	public String toString() {
		return Integer.toString(getRawOp());
	}
}
