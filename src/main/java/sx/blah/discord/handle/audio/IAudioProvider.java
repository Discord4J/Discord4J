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

/**
 * This represents a class which can provide audio to discord.
 */
public interface IAudioProvider {

	/**
	 * This is called to determine whether the audio provider is ready to provide audio or not. When it is not ready, no
	 * audio data queries will be attempted.
	 *
	 * @return True if ready, false if otherwise.
	 */
	boolean isReady();

	/**
	 * This is called to retrieve the actual audio data. This should attempt to return 20 ms of data. The audio must
	 * also respect the encoding type returned by {@link #getAudioEncodingType()}.
	 *
	 * @return The audio data.
	 */
	byte[] provide();

	/**
	 * Gets the number of channels in this audio. NOTE: This only matters if {@link #getAudioEncodingType()} does not
	 * return {@link AudioEncodingType#OPUS}.
	 *
	 * @return The number of channels. It returns 2 (stereo) by default.
	 */
	default int getChannels() {
		return 2;
	}

	/**
	 * This is called to determine the type of audio data provided by this provider. This determines how the audio data
	 * is processed.
	 *
	 * @return The audio encoding type. By default this returns {@link AudioEncodingType#PCM}.
	 */
	default AudioEncodingType getAudioEncodingType() {
		return AudioEncodingType.PCM;
	}
}
