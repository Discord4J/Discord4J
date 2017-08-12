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
 * Provides audio to Discord.
 */
public interface IAudioProvider {

	/**
	 * Used to determine whether the audio provider is ready to provide audio. If the provider is not ready, no calls to
	 * {@link #provide()} should be made.
	 *
	 * @return Whether the audio provider is ready to provide audio.
	 */
	boolean isReady();

	/**
	 * Provides the audio data to be sent to Discord.
	 *
	 * @return 20ms of audio encoded according to {@link #getAudioEncodingType()}.
	 */
	byte[] provide();

	/**
	 * Gets the number of channels in the audio being provided by the provider. By default, <code>2</code>.
	 *
	 * @return The number of channels.
	 */
	default int getChannels() {
		return 2;
	}

	/**
	 * Gets the encoding type of the audio being provided by the provider. By default, {@link AudioEncodingType#PCM}.
	 *
	 * @return The encoding type of the audio being provided by the provider.
	 */
	default AudioEncodingType getAudioEncodingType() {
		return AudioEncodingType.PCM;
	}
}
