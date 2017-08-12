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

package sx.blah.discord.api.internal.json.responses.voice;

/**
 * Received when the voice gateway is ready.
 */
public class VoiceReadyResponse {
	/**
	 * The unique ssrc of the bot user.
	 */
	public int ssrc;
	/**
	 * The port audio is sent to.
	 */
	public int port;
	/**
	 * The encryption modes allowed.
	 */
	public String[] modes;
	/**
	 * The heartbeat interval of the gateway.
	 */
	public int heartbeat_interval;
}
