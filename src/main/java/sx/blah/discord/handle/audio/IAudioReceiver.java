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
 * Handles the receiving of audio from Discord.
 */
@FunctionalInterface
public interface IAudioReceiver {

	/**
	 * Called when audio is received.
	 *
	 * @param audio The packet of audio. Format depends on {{@link #getAudioEncodingType()}}
	 * @param user The user this audio was received from.
	 * @param sequence The sequence from the RTP header of this packet.
	 * @param timestamp The timestamp from the RTP header of this packet.
	 */
	void receive(byte[] audio, IUser user, char sequence, int timestamp);

	/**
	 * This is called to determine the type of audio data provided by this receiver. This determines how the audio data
	 * is processed.
	 *
	 * @return The audio encoding type. By default this returns {@link AudioEncodingType#PCM}.
	 */
	default AudioEncodingType getAudioEncodingType() {
		return AudioEncodingType.PCM;
	}
}
