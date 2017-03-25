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

public class AttachmentPartEntry {
	/**
	 * The name of this file
	 */
	private final String fileName;

	/**
	 * The InputStream containing data for this attachment
	 */
	private final InputStream fileData;

	public AttachmentPartEntry(String fileName, InputStream fileData) {
		this.fileName = fileName;
		this.fileData = fileData;
	}

	/**
	 * @return an InputStream containing the data for this attachment
	 */
	public InputStream getFileData() {
		return fileData;
	}

	/**
	 * @return The name of this attachment
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * Creates an AttachmentPartEntry for a given File.
	 * @param file The file for constructing the AttachmentPartEntry.
	 * @return an AttachmentPartEntry
	 * @throws FileNotFoundException
	 */
	public static AttachmentPartEntry from(File file) throws FileNotFoundException {
		return new AttachmentPartEntry(file.getName(), new FileInputStream(file));
	}

	/**
	 * Creates an AttachmentPartEntry array for a given File array.
	 * @param files The files for constructing AttachmentPartEntries.
	 * @return an AttachmentPartEntry for all provided files.
	 * @throws FileNotFoundException
	 */
	public static AttachmentPartEntry[] from(File... files) throws FileNotFoundException {
		AttachmentPartEntry[] entries = new AttachmentPartEntry[files.length];
		for (int i = 0; i < files.length; i++) {
			entries[i] = from(files[i]);
		}
		return entries;
	}
}
