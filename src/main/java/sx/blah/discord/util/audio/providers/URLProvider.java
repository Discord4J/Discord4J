package sx.blah.discord.util.audio.providers;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.net.URL;

/**
 * This extension of {@link AudioInputStreamProvider} attempts to create an {@link AudioInputStream} from the provided
 * url and then provide based on that.
 */
public class URLProvider extends AudioInputStreamProvider {

	private URLProvider(AudioInputStream stream) {
		super(stream);
	}

	public URLProvider(URL url) throws IOException, UnsupportedAudioFileException {
		this(AudioSystem.getAudioInputStream(url));
	}

	public URLProvider(String url) throws IOException, UnsupportedAudioFileException {
		this(new URL(url));
	}
}
