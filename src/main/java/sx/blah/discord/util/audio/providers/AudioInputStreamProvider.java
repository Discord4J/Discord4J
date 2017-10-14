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

package sx.blah.discord.util.audio.providers;

import sx.blah.discord.Discord4J;
import sx.blah.discord.api.internal.DiscordUtils;
import sx.blah.discord.api.internal.OpusUtil;
import sx.blah.discord.handle.audio.AudioEncodingType;
import sx.blah.discord.handle.audio.IAudioProvider;
import sx.blah.discord.util.LogMarkers;

import javax.sound.sampled.AudioInputStream;
import java.io.IOException;

/**
 * An audio provider which wraps an {@link AudioInputStream}.
 */
public class AudioInputStreamProvider implements IAudioProvider {

	/**
	 * The underlying audio stream.
	 */
	private final AudioInputStream stream;
	/**
	 * Whether the stream is closed.
	 */
	private volatile boolean isClosed = false;

	public AudioInputStreamProvider(AudioInputStream stream) {
		this.stream = DiscordUtils.getPCMStream(stream);
	}

	/**
	 * Gets the underlying audio stream.
	 *
	 * @return The underlying audio stream.
	 */
	public AudioInputStream getStream() {
		return stream;
	}

	@Override
	public boolean isReady() {
		try {
			return !isClosed && stream.available() > -1;
		} catch (IOException e) {
			Discord4J.LOGGER.error(LogMarkers.VOICE, "Discord4J Internal Exception", e);
			return false;
		}
	}

	@Override
	public byte[] provide() {
		byte[] audio = new byte[OpusUtil.OPUS_FRAME_SIZE * stream.getFormat().getFrameSize()];
		try {
			int amountRead = stream.read(audio, 0, audio.length);

			if (amountRead > -1) {
				return audio;
			} else {
				isClosed = true;
				stream.close();
			}
		} catch (IOException e) {
			Discord4J.LOGGER.error(LogMarkers.VOICE, "Discord4J Internal Exception", e);
		}
		return new byte[0];
	}

	@Override
	public int getChannels() {
		return stream.getFormat().getChannels();
	}

	@Override
	public AudioEncodingType getAudioEncodingType() {
		return AudioEncodingType.PCM;
	}
}
