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

package sx.blah.discord.handle.audio;

import sx.blah.discord.handle.obj.IUser;

/**
 * Receives audio from Discord.
 */
@FunctionalInterface
public interface IAudioReceiver {

	/**
	 * Called every 20ms while audio is being received.
	 *
	 * @param audio The received audio, encoded according to {@link #getAudioEncodingType()}.
	 * @param user The user the audio was received from.
	 * @param sequence The sequence of the RTP header for the packet.
	 *                 See {@link sx.blah.discord.api.internal.OpusPacket.RTPHeader#sequence}.
	 * @param timestamp The timestamp of the RTP header for the packet.
	 *                  See {@link sx.blah.discord.api.internal.OpusPacket.RTPHeader#timestamp}.
	 */
	void receive(byte[] audio, IUser user, char sequence, int timestamp);

	/**
	 * Gets the encoding type of the audio that should be sent to the receiver.
	 * By default, {@link AudioEncodingType#PCM}.
	 *
	 * @return The encoding type of the audio that should be sent to the receiver.
	 */
	default AudioEncodingType getAudioEncodingType() {
		return AudioEncodingType.PCM;
	}
}
