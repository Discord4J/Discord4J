package sx.blah.discord.util.audio.providers;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

/**
 * This extension of {@link AudioInputStreamProvider} attempts to create an {@link AudioInputStream} from the provided
 * file and then provide based on that.
 */
public class FileProvider extends AudioInputStreamProvider {

	private FileProvider(AudioInputStream stream) {
		super(stream);
	}

	public FileProvider(File file) throws IOException, UnsupportedAudioFileException {
		this(AudioSystem.getAudioInputStream(file));
	}

	public FileProvider(String pathToFile) throws IOException, UnsupportedAudioFileException {
		this(new File(pathToFile));
	}
}
