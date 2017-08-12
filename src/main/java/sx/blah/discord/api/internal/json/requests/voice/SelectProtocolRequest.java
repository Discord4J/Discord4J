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

/**
 * Sent to select the voice protocol on the voice gateway.
 */
public class SelectProtocolRequest {
	/**
	 * The protocol to use.
	 */
	private String protocol = "udp";
	/**
	 * Inner data object.
	 */
	private Data data;

	public SelectProtocolRequest(String address, int port) {
		this.data = new Data(address, port);
	}

	/**
	 * Inner data object sent with the request
	 */
	private static class Data {
		/**
		 * The local IP address to send to.
		 */
		private String address;
		/**
		 * The port to send on.
		 */
		private int port;
		/**
		 * The encryption mode to use.
		 */
		private String mode = "xsalsa20_poly1305";

		private Data(String address, int port) {
			this.address = address;
			this.port = port;
		}
	}
}
