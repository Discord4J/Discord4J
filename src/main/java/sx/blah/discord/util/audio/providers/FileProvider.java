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

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

/**
 * An audio provider which extracts an {@link AudioInputStream} from a file.
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
