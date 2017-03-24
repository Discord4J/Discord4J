package sx.blah.discord.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * @author ashley
 * @since 3/24/17 4:23 PM
 */
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
