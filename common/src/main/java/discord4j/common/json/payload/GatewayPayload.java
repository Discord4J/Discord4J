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
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import discord4j.common.jackson.PayloadTypeIdResolver;

import java.util.Objects;

public class GatewayPayload {

	@JsonProperty("op")
	private int op;
	@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, property = "op", visible = true,
			include = JsonTypeInfo.As.EXTERNAL_PROPERTY, defaultImpl = Void.class)
	@JsonTypeIdResolver(PayloadTypeIdResolver.class)
	@JsonProperty("d")
	private Payload data;
	@JsonProperty("s")
	private Integer sequence;
	@JsonProperty("t")
	private String type;

	public int getOp() {
		return op;
	}

	public Payload getData() {
		return data;
	}

	public Integer getSequence() {
		return sequence;
	}

	public String getType() {
		return type;
	}

	public void setOp(int op) {
		this.op = op;
	}

	public void setData(Payload data) {
		this.data = data;
	}

	public void setSequence(Integer sequence) {
		this.sequence = sequence;
	}

	public void setType(String type) {
		this.type = type;
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
