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
package discord4j.common;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class GatewayPayload {

	private final int op;
	private final Object d;
	private final Integer s;
	private final String t;

	public GatewayPayload(@JsonProperty("op") int op, @JsonProperty("d") Object d, @JsonProperty("s") Integer s, @JsonProperty("t") String t) {
		this.op = op;
		this.d = d;
		this.s = s;
		this.t = t;
	}

	public static GatewayPayload of(int op, Object d, Integer s, String t) {
		return new GatewayPayload(op, d, s, t);
	}

	public int getOp() {
		return op;
	}

	public Object getD() {
		return d;
	}

	public Integer getS() {
		return s;
	}

	public String getT() {
		return t;
	}

	@Override
	public int hashCode() {
		return Objects.hash(op, d, s, t);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || obj.getClass() != GatewayPayload.class) {
			return false;
		}

		GatewayPayload other = (GatewayPayload) obj;

		return this.op == other.op
				&& this.d.equals(other.d)
				&& Objects.equals(this.s, other.s)
				&& Objects.equals(this.t, other.t);
	}

	@Override
	public String toString() {
		return "GatewayPayload[op=" + op + ", d=" + d + ", s=" + s + ", t=" + t + "]";
	}
}
