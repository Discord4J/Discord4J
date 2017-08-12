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

package sx.blah.discord.util.audio.processors;

import org.tritonus.dsp.ais.AmplitudeAudioInputStream;
import sx.blah.discord.Discord4J;
import sx.blah.discord.handle.audio.AudioEncodingType;
import sx.blah.discord.handle.audio.IAudioProcessor;
import sx.blah.discord.handle.audio.IAudioProvider;
import sx.blah.discord.util.LogMarkers;
import sx.blah.discord.util.audio.providers.AudioInputStreamProvider;

import java.io.IOException;

/**
 * An audio processor which allows for change in volume.
 *
 * <p>This processor <b>ONLY</b> compatible with {@link sx.blah.discord.util.audio.providers.AudioInputStreamProvider}.
 */
public class VolumeProcessor implements IAudioProcessor {

	/**
	 * The audio stream which wraps the audio input stream provider and changes the volume of the audio data.
	 */
	private volatile AmplitudeAudioInputStream stream;
	/**
	 * The audio provider wrapped by the processor.
	 */
	private volatile IAudioProvider provider = null;
	/**
	 * The volume of the processor.
	 */
	private volatile float volume = 1.0F;

	/**
	 * Gets the volume.
	 *
	 * @return The volume.
	 */
	public float getVolume() {
		return volume;
	}

	/**
	 * Sets the volume.
	 *
	 * @param volume The new volume (1.0 is the default value).
	 */
	public void setVolume(float volume) {
		this.volume = volume;
		stream.setAmplitudeLinear(volume);
	}

	@Override
	public boolean setProvider(IAudioProvider provider) {
		if (stream != null) {
			try {
				stream.close();
			} catch (IOException e) {
				Discord4J.LOGGER.error(LogMarkers.VOICE, "Discord4J Internal Exception", e);
			}
		}

		if (provider instanceof AudioInputStreamProvider) {
			stream = new AmplitudeAudioInputStream(((AudioInputStreamProvider) provider).getStream());
			this.provider = new AudioInputStreamProvider(stream);
			return true;
		}

		this.provider = null;
		stream = null;
		return false;
	}

	@Override
	public boolean isReady() {
		return provider != null && provider.isReady();
	}

	@Override
	public byte[] provide() {
		return provider.provide();
	}

	@Override
	public int getChannels() {
		return provider.getChannels();
	}

	@Override
	public AudioEncodingType getAudioEncodingType() {
		return provider.getAudioEncodingType();
	}
}
