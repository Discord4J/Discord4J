package sx.blah.discord.api.internal;

import sun.net.spi.nameservice.dns.DNSNameService;
import sx.blah.discord.Discord4J;
import sx.blah.discord.util.LogMarkers;

import javax.sound.sampled.spi.AudioFileReader;
import javax.sound.sampled.spi.FormatConversionProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

public class Services {
	static void load() {
		try {
			File servicesJar = Files.createTempFile("discord4j-services", "jar").toFile();
			servicesJar.deleteOnExit();

			Map<Class, List<String>> services = new HashMap<>();
			services.put(FormatConversionProvider.class, Arrays.asList(
					"javazoom.spi.mpeg.sampled.convert.MpegFormatConversionProvider",
					"org.kc7bfi.jflac.sound.spi.FlacFormatConversionProvider"));

			services.put(AudioFileReader.class, Arrays.asList(
					"javazoom.spi.mpeg.sampled.file.MpegAudioFileReader",
					"org.kc7bfi.jflac.sound.spi.FlacAudioFileReader"));

			try (JarOutputStream jarOut = new JarOutputStream(new FileOutputStream(servicesJar))) {
				for (Map.Entry<Class, List<String>> entry : services.entrySet()) {
					String fileName = "META-INF/services" + entry.getKey().getCanonicalName();
					ZipEntry zipEntry = new ZipEntry(fileName);
					jarOut.putNextEntry(zipEntry);

					for (String provider : entry.getValue()) jarOut.write((provider + "\n").getBytes());
					jarOut.closeEntry();
				}
			}

			ClassLoader extLoader = DNSNameService.class.getClassLoader();
			Method addExtUrl = extLoader.getClass().getDeclaredMethod("addExtURL", URL.class);
			addExtUrl.setAccessible(true);
			addExtUrl.invoke(extLoader, servicesJar.toURI().toURL());

			for (Class service : services.keySet()) {
				ServiceLoader loader = ServiceLoader.load(service);
				loader.reload();
			}
		} catch (Exception e) {
			Discord4J.LOGGER.error(LogMarkers.API, "Encountered exception loading audio SPIs: ", e);
		}
 	}
}
