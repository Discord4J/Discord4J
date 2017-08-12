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

package sx.blah.discord.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * A pair of a file name and file data used for sending files to Discord.
 *
 * @see sx.blah.discord.handle.obj.IChannel#sendFile(File) Channel sendFile methods
 */
public class AttachmentPartEntry {
	/**
	 * The name of the file that will be shown in Discord.
	 */
	private final String fileName;

	/**
	 * The stream of data that can be read and sent to Discord.
	 */
	private final InputStream fileData;

	public AttachmentPartEntry(String fileName, InputStream fileData) {
		this.fileName = fileName;
		this.fileData = fileData;
	}

	/**
	 * Gets the stream of data that can be read and sent to Discord.
	 *
	 * @return The stream of data that can be read and sent to Discord.
	 */
	public InputStream getFileData() {
		return fileData;
	}

	/**
	 * Gets the name of the file that will be shown in Discord.
	 *
	 * @return The name of the file that will be shown in Discord.
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * Creates an attachment part entry from a file.
	 *
	 * @param file The file to get a name and data from.
	 * @return An attachment part entry with the given file's name and data.
	 *
	 * @throws FileNotFoundException If the file cannot be found.
	 */
	public static AttachmentPartEntry from(File file) throws FileNotFoundException {
		return new AttachmentPartEntry(file.getName(), new FileInputStream(file));
	}

	/**
	 * Creates an array of attachment part entries from an array of files.
	 *
	 * @param files The files to get name and data from.
	 * @return An array of attachment part entries with the given files' names and data.
	 *
	 * @throws FileNotFoundException If one of the files cannot be found.
	 */
	public static AttachmentPartEntry[] from(File... files) throws FileNotFoundException {
		AttachmentPartEntry[] entries = new AttachmentPartEntry[files.length];
		for (int i = 0; i < files.length; i++) {
			entries[i] = from(files[i]);
		}
		return entries;
	}
}
