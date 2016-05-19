package sx.blah.discord.util.audio.providers;

import sx.blah.discord.Discord4J;
import sx.blah.discord.handle.audio.IAudioProvider;
import sx.blah.discord.handle.audio.impl.AudioManager;
import sx.blah.discord.util.LogMarkers;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.IOException;

/**
 * This represents an {@link IAudioProvider} implementation which wraps an {@link AudioInputStream}.
 */
public class AudioInputStreamProvider implements IAudioProvider {

	private final AudioInputStream stream;
	private volatile boolean isClosed = false;

	public AudioInputStreamProvider(AudioInputStream stream) {
		AudioFormat baseFormat = stream.getFormat();

		//Converts first to PCM data. If the data is already PCM data, this will not change anything.
		AudioFormat toPCM = new AudioFormat(
				AudioFormat.Encoding.PCM_SIGNED,
				baseFormat.getSampleRate(),//AudioConnection.OPUS_SAMPLE_RATE,
				baseFormat.getSampleSizeInBits() != -1 ? baseFormat.getSampleSizeInBits() : 16,
				baseFormat.getChannels(),
				//If we are given a frame size, use it. Otherwise, assume 16 bits (2 8bit shorts) per channel.
				baseFormat.getFrameSize() != -1 ? baseFormat.getFrameSize() : 2*baseFormat.getChannels(),
				baseFormat.getFrameRate() != -1 ? baseFormat.getFrameRate() : baseFormat.getSampleRate(),
				baseFormat.isBigEndian());
		AudioInputStream pcmStream = AudioSystem.getAudioInputStream(toPCM, stream);

		//Then resamples to a sample rate of 48000hz and ensures that data is Big Endian.
		AudioFormat audioFormat = new AudioFormat(
				toPCM.getEncoding(),
				AudioManager.OPUS_SAMPLE_RATE,
				toPCM.getSampleSizeInBits(),
				toPCM.getChannels(),
				toPCM.getFrameSize(),
				toPCM.getFrameRate(),
				true);

		this.stream = AudioSystem.getAudioInputStream(audioFormat, pcmStream);
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
