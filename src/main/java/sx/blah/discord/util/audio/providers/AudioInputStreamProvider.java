package sx.blah.discord.util.audio.providers;

import sx.blah.discord.Discord4J;
import sx.blah.discord.api.internal.DiscordUtils;
import sx.blah.discord.handle.audio.IAudioProvider;
import sx.blah.discord.handle.audio.impl.AudioManager;
import sx.blah.discord.util.LogMarkers;

import javax.sound.sampled.AudioInputStream;
import java.io.IOException;

/**
 * This represents an {@link IAudioProvider} implementation which wraps an {@link AudioInputStream}.
 */
public class AudioInputStreamProvider implements IAudioProvider {

	private final AudioInputStream stream;
	private volatile boolean isClosed = false;

	public AudioInputStreamProvider(AudioInputStream stream) {
		this.stream = DiscordUtils.getPCMStream(stream);
	}

	/**
	 * Gets the stream associated with this provider.
	 *
	 * @return The stream.
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
		byte[] audio = new byte[AudioManager.OPUS_FRAME_SIZE*stream.getFormat().getFrameSize()];
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
