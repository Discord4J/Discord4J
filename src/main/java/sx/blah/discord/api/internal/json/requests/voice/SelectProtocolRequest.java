/*
 *     This file is part of Discord4J.
 *
 *     Discord4J is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Discord4J is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */

package sx.blah.discord.api.internal.json.requests.voice;

public class SelectProtocolRequest {
	private String protocol = "udp";
	private Data data;

	public SelectProtocolRequest(String address, int port) {
		this.data = new Data(address, port);
	}

	private static class Data {
		private String address;
		private int port;
		private String mode = "xsalsa20_poly1305";

		private Data(String address, int port) {
			this.address = address;
			this.port = port;
		}
	}
}
