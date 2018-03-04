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

package sx.blah.discord.modules;

import org.apache.commons.io.filefilter.FileFilterUtils;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.module.ModuleDisabledEvent;
import sx.blah.discord.handle.impl.events.module.ModuleEnabledEvent;
import sx.blah.discord.util.LogMarkers;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

/**
 * Manages loading and unloading modules for a discord client.
 */
public class ModuleLoader {

	/**
	 * The directory external modules are located in.
	 */
	public static final String MODULE_DIR = "modules";
	/**
	 * The classes of the modules loaded by the loader.
	 */
	protected static final List<Class<? extends IModule>> modules = new CopyOnWriteArrayList<>();

	/**
	 * The client which owns the module loader.
	 */
	private IDiscordClient client;
	/**
	 * The modules loaded by the loader.
	 */
	private List<IModule> loadedModules = new CopyOnWriteArrayList<>();

	static {
		// Yay! Proprietary hooks. This is used for ModuleLoader+ (https://github.com/Discord4J-Addons/Module-Loader-Plus)
		// to be able to load internal modules automagically. This is not in Discord4J by default due to the massive
		// overhead it provides.
		try {
			Class.forName("com.austinv11.modules.ModuleLoaderPlus"); // Loads the class' static initializer block
		} catch (ClassNotFoundException ignored) {}

		if (Configuration.LOAD_EXTERNAL_MODULES) {
			File modulesDir = new File(MODULE_DIR);
			if (modulesDir.exists()) {
				if (!modulesDir.isDirectory()) {
					throw new RuntimeException(MODULE_DIR + " isn't a directory!");
				}
			} else {
				if (!modulesDir.mkdir()) {
					throw new RuntimeException("Error creating " + MODULE_DIR + " directory");
				}
			}

			File[] files = modulesDir.listFiles((FilenameFilter) FileFilterUtils.suffixFileFilter("jar"));
			if (files != null && files.length > 0) {
				Discord4J.LOGGER.info(LogMarkers.MODULES, "Attempting to load {} external module(s)...", files.length);
				loadExternalModules(new ArrayList<>(Arrays.asList(files)));
			}
		}
	}

	public ModuleLoader(IDiscordClient client) {
		this.client = client;

		for (Class<? extends IModule> clazz : modules) {
			try {
				IModule module = clazz.newInstance();
				Discord4J.LOGGER.info(LogMarkers.MODULES, "Loading module {} v{} by {}", module.getName(), module.getVersion(), module.getAuthor());
				if (canModuleLoad(module)) {
					loadedModules.add(module);
				} else {
					Discord4J.LOGGER.warn(LogMarkers.MODULES, "Skipped loading of module {} (expected Discord4J v{} instead of v{})", module.getName(), module.getMinimumDiscord4JVersion(), Discord4J.VERSION);
				}
			} catch (InstantiationException | IllegalAccessException e) {
				Discord4J.LOGGER.error(LogMarkers.MODULES, "Unable to load module " + clazz.getName() + "!", e);
			}
		}
	}

	/**
	 * Gets the modules loaded by the module loader.
	 *
	 * @return The modules loaded.
	 */
	public List<IModule> getLoadedModules() {
		return loadedModules;
	}

	/**
	 * Gets the module classes which will be or have been loaded. These may or may not be enabled in a given module
	 * instance.
	 *
	 * @return The module classes.
	 */
	public static List<Class<? extends IModule>> getModules() {
		return modules;
	}

	/**
	 * Attempts to {@link #loadModule(IModule) load} all {@link #getLoadedModules() loaded modules}.
	 */
	public void loadModules() {
		List<IModule> toLoad = new CopyOnWriteArrayList<>(loadedModules);
		while (toLoad.size() > 0) {
			for (IModule module : toLoad) {
				if (loadModule(module))
					toLoad.remove(module);
			}
		}
	}

	/**
	 * Manually loads a module.
	 *
	 * @param module The module to load.
	 * @return Whether the module was successfully loaded.
	 */
	public boolean loadModule(IModule module) {
                if (!loadedModules.contains(module) && !canModuleLoad(module)) {
			return false;
                }
		Class<? extends IModule> clazz = module.getClass();
		if (clazz.isAnnotationPresent(Requires.class)) {
			Requires annotation = clazz.getAnnotation(Requires.class);
			if (!hasDependency(loadedModules, annotation.value())) {
				return false;
			}
		}
		boolean enabled = module.enable(client);
		if (enabled) {
			client.getDispatcher().registerListener(module);
			if (!loadedModules.contains(module))
				loadedModules.add(module);

			client.getDispatcher().dispatch(new ModuleEnabledEvent(module));
		}

		return true;
	}

	/**
	 * Manually unloads a module.
	 *
	 * @param module The module to unload.
	 */
	public void unloadModule(IModule module) {
		loadedModules.remove(module);
		module.disable();
		client.getDispatcher().unregisterListener(module);

		loadedModules.removeIf(mod -> {
			Class<? extends IModule> clazz = module.getClass();
			if (clazz.isAnnotationPresent(Requires.class)) {
				Requires annotation = clazz.getAnnotation(Requires.class);
				if (annotation.value().equals(module.getClass().getName())) {
					unloadModule(mod);
					return true;
				}
			}
			return false;
		});

		client.getDispatcher().dispatch(new ModuleDisabledEvent(module));
	}

	/**
	 * Gets whether the given list of modules has a module with the given class name.
	 *
	 * @param modules The modules to check.
	 * @param className The class name to search for.
	 * @return Whether the given list of modules has a module with the given class name.
	 */
	private boolean hasDependency(List<IModule> modules, String className) {
		for (IModule module : modules)
			if (module.getClass().getName().equals(className))
				return true;
		return false;
	}

	/**
	 * Gets whether the given module can be loaded.
	 *
	 * @param module The module to check.
	 * @return Whether the given module can be loaded.
	 */
	private boolean canModuleLoad(IModule module) {
		String[] versions;
		String[] discord4jVersion;
		try {
			versions = module.getMinimumDiscord4JVersion().toLowerCase(Locale.ROOT).replace("-snapshot", "").split("\\.");
			discord4jVersion = Discord4J.VERSION.toLowerCase(Locale.ROOT).replace("-snapshot", "").split("\\.");

			for (int i = 0; i < Math.min(versions.length, 2); i++) { // We only care about major.minor, the revision change should not be big enough to care about
				if (Integer.parseInt(versions[i]) > Integer.parseInt(discord4jVersion[i]))
					return false;
			}
		} catch (NumberFormatException e) {
			Discord4J.LOGGER.error(LogMarkers.MODULES, "Module {} has incorrect minimum Discord4J version syntax! ({})", module.getName(), module.getMinimumDiscord4JVersion());
			return false;
		}
		return true;
	}

	/**
	 * Loads a jar file and automatically adds any modules.
	 * To avoid high overhead recursion, specify the attribute "Discord4J-ModuleClass" in your jar manifest.
	 * Multiple classes should be separated by a semicolon ";".
	 *
	 * @param file The jar file to load.
	 */
	public static synchronized void loadExternalModules(File file) { // A bit hacky, but oracle be dumb and encapsulates URLClassLoader#addUrl()
		if (file.isFile() && file.getName().endsWith(".jar")) { // Can't be a directory and must be a jar
			try (JarFile jar = new JarFile(file)) {
				Manifest man = jar.getManifest();
				String moduleAttrib = man == null ? null : man.getMainAttributes().getValue("Discord4J-ModuleClass");
				String[] moduleClasses = new String[0];
				if (moduleAttrib != null) {
					moduleClasses = moduleAttrib.split(";");
				}
				// Executes would should be URLCLassLoader.addUrl(file.toURI().toURL());
				URLClassLoader loader = (URLClassLoader) ClassLoader.getSystemClassLoader();
				URL url = file.toURI().toURL();
				for (URL it : Arrays.asList(loader.getURLs())) { // Ensures duplicate libraries aren't loaded
					if (it.equals(url)) {
						return;
					}
				}
				Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
				method.setAccessible(true);
				method.invoke(loader, url);
				if (moduleClasses.length == 0) { // If the Module Developer has not specified the Implementing Class, revert to recursive search
					// Scans the jar file for classes which have IModule as a super class
					List<String> classes = new ArrayList<>();
					jar.stream().filter(jarEntry -> !jarEntry.isDirectory() && jarEntry.getName().endsWith(".class")).map(path -> path.getName().replace('/', '.').substring(0, path.getName().length() - ".class".length())).forEach(classes::add);
					for (String clazz : classes) {
						try {
							Class classInstance = loadClass(clazz);
							if (IModule.class.isAssignableFrom(classInstance) && !classInstance.equals(IModule.class)) {
								addModuleClass(classInstance);
							}
						} catch (NoClassDefFoundError ignored) { /* This can happen. Looking recursively looking through the classpath is hackish... */ }
					}
				} else {
					for (String moduleClass : moduleClasses) {
						Discord4J.LOGGER.info(LogMarkers.MODULES, "Loading Class from Manifest Attribute: {}", moduleClass);
						Class classInstance = loadClass(moduleClass);
						if (IModule.class.isAssignableFrom(classInstance))
							addModuleClass(classInstance);
					}
				}
			} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | IOException | ClassNotFoundException e) {
				Discord4J.LOGGER.error(LogMarkers.MODULES, "Unable to load module " + file.getName() + "!", e);
			}
		}
	}

	/**
	 * Recursively loads the parents of subclasses in order to avoid class loader errors.
	 */
	private static Class loadClass(String clazz) throws ClassNotFoundException {
		if (clazz.contains("$") && clazz.substring(0, clazz.lastIndexOf("$")).length() > 0) {
			try {
				loadClass(clazz.substring(0, clazz.lastIndexOf("$")));
			} catch (ClassNotFoundException ignored) {
			} // If the parent class doesn't exist then it is safe to instantiate the child
		}
		return Class.forName(clazz);
	}

	/**
	 * Loads a list of jar files and automatically resolves any dependency issues.
	 *
	 * @param files The jar files to load.
	 */
	public static void loadExternalModules(List<File> files) {
		List<File> independents = new ArrayList<>();
		List<File> dependents = new ArrayList<>();

		files.forEach((file) -> {
			try {
				if (getModuleRequires(file).length > 0) {
					dependents.add(file);
				} else {
					independents.add(file);
				}
			} catch (IOException e) {
				Discord4J.LOGGER.error(LogMarkers.MODULES, "Discord4J Internal Exception");
			}
		});

		independents.forEach(ModuleLoader::loadExternalModules);

		List<File> noLongerDependents = dependents.stream().filter(jarFile -> { // loads all dependents whose requirements have been met already
			try {
				String[] moduleRequires = getModuleRequires(jarFile);
				List<Class> classes = new ArrayList<>();
				for (String clazz : moduleRequires) {
					classes.add(Class.forName(clazz));
				}
				return classes.size() == moduleRequires.length;
			} catch (Exception e) {
				return false;
			}
		}).collect(Collectors.toList());
		dependents.removeAll(noLongerDependents);
		noLongerDependents.forEach(ModuleLoader::loadExternalModules);

		final int retryAttempts = dependents.size();
		for (int i = 0; i < retryAttempts; i++) {
			dependents.removeIf((file -> { // Filters out all usable files
				boolean loaded = false;
				try {
					String[] required = getModuleRequires(file);
					for (String clazz : required) {
						try {
							Class.forName(clazz);
							loaded = true;
						} catch (ClassNotFoundException ignored) {}

						if (!loaded)
							loaded = findFileForClass(files, clazz) != null;

						if (!loaded)
							break;
					}
				} catch (IOException ignored) {}

				if (loaded)
					loadExternalModules(file);

				return loaded;
			}));

			if (dependents.isEmpty())
				break;
		}

		if (dependents.size() > 0)
			Discord4J.LOGGER.warn(LogMarkers.MODULES, "Unable to load {} modules!", dependents.size());
	}

	/**
	 * Gets the <code>Module-Requires</code> attribute list from the given jar file manifest.
	 *
	 * @param file The jar file to extract the manifest attribute from.
	 * @return The value of the attribute.
	 * @throws IOException If the jar file read operation fails.
	 */
	private static String[] getModuleRequires(File file) throws IOException {
		JarFile jarFile = new JarFile(file);
		Manifest manifest = jarFile.getManifest();
		Attributes.Name moduleRequiresLower = new Attributes.Name("module-requires"); //TODO remove
		Attributes.Name moduleRequiresUpper = new Attributes.Name("Module-Requires");
		if (manifest != null && manifest.getMainAttributes() != null //TODO remove
				&& manifest.getMainAttributes().containsKey(moduleRequiresLower)) {
			String value = manifest.getMainAttributes().getValue(moduleRequiresLower);
			Discord4J.LOGGER.warn(LogMarkers.MODULES, "File {} uses the 'module-requires' attribute instead of 'Module-Requires', please rename the attribute!", file.getName());
			return value.contains(";") ? value.split(";") : new String[]{value};
		} else if (manifest != null && manifest.getMainAttributes() != null
				&& manifest.getMainAttributes().containsKey(moduleRequiresUpper)) {
			String value = manifest.getMainAttributes().getValue(moduleRequiresUpper);
			return value.contains(";") ? value.split(";") : new String[]{value};
		} else {
			return new String[0];
		}
	}

	/**
	 * Gets the jar file which contains a class with the given class name.
	 *
	 * @param files The jar files to search.
	 * @param clazz The class name to search for.
	 * @return The jar file which contains a class with the given class name (or null if one was not found).
	 */
	private static File findFileForClass(List<File> files, String clazz) {
		return files.stream().filter((file) -> {
			try {
				JarFile jarFile = new JarFile(file);
				return jarFile.getJarEntry(clazz.replaceAll("\\.", File.pathSeparator) + ".class") != null;
			} catch (IOException e) {
				return false;
			}
		}).findFirst().orElse(null);
	}

	/**
	 * Manually adds a module class to be considered for loading.
	 *
	 * @param clazz The module class.
	 */
	public static void addModuleClass(Class<? extends IModule> clazz) {
                if (!Modifier.isAbstract(clazz.getModifiers())
                        && !Modifier.isInterface(clazz.getModifiers())
                        && !modules.contains(clazz)) {
                        modules.add(clazz);
                }
	}
}
