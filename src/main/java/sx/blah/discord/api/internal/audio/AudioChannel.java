package sx.blah.discord.api.internal.audio;

import sx.blah.discord.api.internal.DiscordVoiceWS;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class AudioChannel {

	private static List<AudioInputStream> audioQueue = new ArrayList<>();

	public static void queueUrl(String url) {
		try {
			queueUrl(new URL(url));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	public static void queueUrl(URL url) {
		try {
			BufferedInputStream bis = new BufferedInputStream(url.openStream());
			queue(AudioSystem.getAudioInputStream(bis));
		} catch (IOException | UnsupportedAudioFileException e) {
			e.printStackTrace();
		}
	}


	public static void queueFile(String file) {
		queueFile(new File(file));
	}

	public static void queueFile(File file) {
		try {
			queue(AudioSystem.getAudioInputStream(file));
		} catch (UnsupportedAudioFileException | IOException e) {
			e.printStackTrace();
		}
	}

	public static void queue(AudioInputStream inSource) {
		if (inSource == null)
			throw new IllegalArgumentException("Cannot create an audio player from a null AudioInputStream!");


		AudioFormat baseFormat = inSource.getFormat();

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
		AudioInputStream pcmStream = AudioSystem.getAudioInputStream(toPCM, inSource);

		//Then resamples to a sample rate of 48000hz and ensures that data is Big Endian.
		AudioFormat audioFormat = new AudioFormat(
				toPCM.getEncoding(),
				DiscordVoiceWS.OPUS_SAMPLE_RATE,
				toPCM.getSampleSizeInBits(),
				toPCM.getChannels(),
				toPCM.getFrameSize(),
				toPCM.getFrameRate(),
				true);

		AudioInputStream outputAudio = AudioSystem.getAudioInputStream(audioFormat, pcmStream);

		if (outputAudio != null)
			audioQueue.add(outputAudio);
	}


	/**
	 * Gets the PCM data that needs to be sent.
	 *
	 * @param length : How many MS of data needed to be sent.
	 */
	public static byte[] getAudioData(int length) {
		AudioInputStream data = audioQueue.get(0);
		if (data != null) {
			try {
				int amountRead;
				byte[] audio = new byte[length*data.getFormat().getFrameSize()];

				amountRead = data.read(audio, 0, audio.length);

				if (amountRead > 0) {
					return audio;
				} else {
					audioQueue.remove(0);
					return getAudioData(length);
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
