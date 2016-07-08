package sx.blah.discord.util.audio.processors;

import org.tritonus.dsp.ais.AmplitudeAudioInputStream;
import sx.blah.discord.Discord4J;
import sx.blah.discord.handle.audio.IAudioProcessor;
import sx.blah.discord.handle.audio.IAudioProvider;
import sx.blah.discord.util.LogMarkers;
import sx.blah.discord.util.audio.providers.AudioInputStreamProvider;

import java.io.IOException;

/**
 * This processor allows for the volume of {@link sx.blah.discord.util.audio.providers.AudioInputStreamProvider}s to be
 * changed. NOTE: This is ONLY compatible with {@link sx.blah.discord.util.audio.providers.AudioInputStreamProvider}.
 */
public class VolumeProcessor implements IAudioProcessor {

	private volatile AmplitudeAudioInputStream stream;
	private volatile IAudioProvider provider = null;
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
