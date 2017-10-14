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

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import sx.blah.discord.Discord4J;
import sx.blah.discord.handle.obj.IUser;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

/**
 * A function which supplies base64-encoded image data for use as images in Discord.
 */
@FunctionalInterface
public interface Image {

	/**
	 * Gets the base64-encoded encoded image.
	 *
	 * @return The nullable base64-encoded image.
	 */
	String getData();

	/**
	 * Gets the image data for a user's avatar.
	 *
	 * @param user The user to get the image data from.
	 * @return The user's avatar image.
	 */
	static Image forUser(IUser user) {
		return user::getAvatar;
	}

	/**
	 * Gets the data (null) for the default Discord avatar.
	 *
	 * @return The default avatar image.
	 */
	static Image defaultAvatar() {
		return () -> null;
	}

	/**
	 * Generates an avatar image from bytes representing an image.
	 *
	 * @param imageType The image type, ex. jpeg, png, etc.
	 * @param data The image's bytes.
	 * @return The avatar image.
	 */
	static Image forData(String imageType, byte[] data) {
		return () -> String.format("data:image/%s;base64,%s", imageType, Base64.encodeBase64String(data));
	}

	/**
	 * Generates an avatar image from an input stream representing an image.
	 *
	 * @param imageType The image type, ex. jpeg, png, etc.
	 * @param stream The image's input stream.
	 * @return The avatar image.
	 */
	static Image forStream(String imageType, InputStream stream) {
		return () -> {
			try {
				Image image = forData(imageType, IOUtils.toByteArray(stream));
				stream.close();
				return image.getData();
			} catch (Exception e) {
				Discord4J.LOGGER.error(LogMarkers.UTIL, "Discord4J Internal Exception", e);
			}
			return defaultAvatar().getData();
		};
	}

	/**
	 * Generates an avatar image from a direct link to an image.
	 *
	 * @param imageType The image type, ex. jpeg, png, etc.
	 * @param url The direct link to an image.
	 * @return The avatar image.
	 */
	static Image forUrl(String imageType, String url) {
		return () -> {
			try {
				URLConnection urlConnection = new URL(url).openConnection();
				urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36");
				InputStream stream = urlConnection.getInputStream();
				return forStream(imageType, stream).getData();
			} catch (IOException e) {
				Discord4J.LOGGER.error(LogMarkers.UTIL, "Discord4J Internal Exception", e);
			}
			return defaultAvatar().getData();
		};
	}

	/**
	 * Generates an avatar image from a file.
	 *
	 * @param file The image file.
	 * @return The avatar image.
	 */
	static Image forFile(File file) {
		return () -> {
			String imageType = FilenameUtils.getExtension(file.getName());
			try {
				return forStream(imageType, new FileInputStream(file)).getData();
			} catch (FileNotFoundException e) {
				Discord4J.LOGGER.error(LogMarkers.UTIL, "Discord4J Internal Exception", e);
			}
			return defaultAvatar().getData();
		};
	}
}
