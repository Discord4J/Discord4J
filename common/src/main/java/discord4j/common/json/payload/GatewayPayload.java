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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import discord4j.common.jackson.PayloadDeserializer;

import javax.annotation.Nullable;
import java.util.Objects;

@JsonDeserialize(using = PayloadDeserializer.class)
public class GatewayPayload {

	private int op;
	@JsonProperty("d")
	private Payload data;
	@JsonProperty("s")
	@Nullable
	private Integer sequence;
	@JsonProperty("t")
	@Nullable
	private String type;

	public GatewayPayload(int op, Payload data, @Nullable Integer sequence, @Nullable String type) {
		this.op = op;
		this.data = data;
		this.sequence = sequence;
		this.type = type;
	}

	public GatewayPayload() {
	}

	public int getOp() {
		return op;
	}

	public Payload getData() {
		return data;
	}

	@Nullable
	public Integer getSequence() {
		return sequence;
	}

	@Nullable
	public String getType() {
		return type;
	}

	@Override
	public int hashCode() {
		return Objects.hash(op, data, sequence, type);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || obj.getClass() != GatewayPayload.class) {
			return false;
		}

		GatewayPayload other = (GatewayPayload) obj;

		return this.op == other.op
				&& this.data.equals(other.data)
				&& Objects.equals(this.sequence, other.sequence)
				&& Objects.equals(this.type, other.type);
	}

	@Override
	public String toString() {
		return "GatewayPayload[op=" + op + ", data=" + data + ", sequence=" + sequence + ", type=" + type + "]";
	}
}
