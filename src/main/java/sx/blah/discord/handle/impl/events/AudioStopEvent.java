package sx.blah.discord.handle.impl.events;

import sx.blah.discord.api.Event;
import sx.blah.discord.handle.AudioChannel;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import java.io.File;
import java.net.URL;
import java.util.Optional;

/**
 * This event is fired when an audio stream stops to playing from the bot.
 */
public class AudioStopEvent extends Event {

	/**
	 * The audio stream queued
	 */
	private final AudioInputStream stream;
	/**
	 * The file source (if present)
	 */
	private final Optional<File> fileSource;
	/**
	 * The url source (if present)
	 */
	private final Optional<URL> urlSource;
	/**
	 * The audio file format
	 */
	private final AudioFileFormat format;

	/**
	 * The Audio Channel
	 */
	private final AudioChannel audioChannel;

	public AudioStopEvent(AudioInputStream stream, File fileSource, URL urlSource, AudioFileFormat format, AudioChannel audioChannel) {
		this.stream = stream;
		this.format = format;
		this.fileSource = Optional.ofNullable(fileSource);
		this.urlSource = Optional.ofNullable(urlSource);
		this.audioChannel = audioChannel;
	}

	/**
	 * Gets the audio stream for the queued audio.
	 *
	 * @return The audio stream.
	 */
	public AudioInputStream getStream() {
		return stream;
	}

	/**
	 * Gets the source file for the queued audio.
	 *
	 * @return The source file (or none if it doesn't exist)
	 */
	public Optional<File> getFileSource() {
		return fileSource;
	}

	/**
	 * Gets the source url for the queued audio.
	 *
	 * @return The source url (or none if it doesn't exist)
	 */
	public Optional<URL> getUrlSource() {
		return urlSource;
	}

	/**
	 * Gets the audio file format for the queued audio.
	 *
	 * @return The file format.
	 */
	public AudioFileFormat getFormat() {
		return format;
	}

	/**
	 * Gets the audio channel for the queued audio.
	 *
	 * @return The audio channel.
	 */
	public AudioChannel getAudioChannel() { return audioChannel; }
}
