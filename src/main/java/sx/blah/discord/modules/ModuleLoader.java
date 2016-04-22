package sx.blah.discord.modules;

import org.apache.commons.io.filefilter.FileFilterUtils;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.ModuleEnabledEvent;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

/**
 * This class is used to manage loading and unloading modules for a discord client.
 */
public class ModuleLoader {

	/**
	 * This is the directory external modules are located in
	 */
	public static final String MODULE_DIR = "modules";
	protected static final List<Class<? extends IModule>> modules = new CopyOnWriteArrayList<>();

	private IDiscordClient client;
	private List<IModule> loadedModules = new CopyOnWriteArrayList<>();

	static {
		if (Configuration.LOAD_EXTERNAL_MODULES) {
			File modulesDir = new File(MODULE_DIR);
			if (modulesDir.exists()) {
				if (!modulesDir.isDirectory()) {
					throw new RuntimeException(MODULE_DIR+" isn't a directory!");
				}
			} else {
				if (!modulesDir.mkdir()) {
					throw new RuntimeException("Error creating "+MODULE_DIR+" directory");
				}
			}

			File[] files = modulesDir.listFiles((FilenameFilter) FileFilterUtils.suffixFileFilter("jar"));
			if (files != null && files.length > 0) {
				Discord4J.LOGGER.info("Attempting to load {} external module(s)...", files.length);
				loadExternalModules(new ArrayList<>(Arrays.asList(files)));
			}
		}
	}

	public ModuleLoader(IDiscordClient client) {
		this.client = client;

		for (Class<? extends IModule> clazz : modules) {
			try {
				IModule module = clazz.newInstance();
				Discord4J.LOGGER.info("Loading module {} v{} by {}", module.getName(), module.getVersion(), module.getAuthor());
				if (canModuleLoad(module)) {
					loadedModules.add(module);
				} else {
					Discord4J.LOGGER.warn("Skipped loading of module {} (expected Discord4J v{} instead of v{})", module.getName(), module.getMinimumDiscord4JVersion(), Discord4J.VERSION);
				}
			} catch (InstantiationException | IllegalAccessException e) {
				Discord4J.LOGGER.error("Unable to load module "+clazz.getName()+"!", e);
			}
		}

		if (Configuration.AUTOMATICALLY_ENABLE_MODULES) {//Handles module load order and loads the modules
			List<IModule> toLoad = new CopyOnWriteArrayList<>(loadedModules);
			while (toLoad.size() > 0) {
				for (IModule module : toLoad) {
					if (loadModule(module))
						toLoad.remove(module);
				}
			}
		}
	}

	/**
	 * Gets the modules loaded in this ModuleLoader instance.
	 *
	 * @return The list of loaded modules.
	 */
	public List<IModule> getLoadedModules() {
		return loadedModules;
	}

	/**
	 * Manually loads a module.
	 *
	 * @param module The module to load.
	 * @return True if the module was successfully loaded, false if otherwise. Note: successful load != successfully enabled
	 */
	public boolean loadModule(IModule module) {
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

		client.getDispatcher().dispatch(new ModuleEnabledEvent(module));
	}

	private boolean hasDependency(List<IModule> modules, String className) {
		for (IModule module : modules)
			if (module.getClass().getName().equals(className))
				return true;
		return false;
	}

	private boolean canModuleLoad(IModule module) {
		String[] versions;
		String[] discord4jVersion;
		try {
			versions = module.getMinimumDiscord4JVersion().toLowerCase().replace("-snapshot", "").split("\\.");
			discord4jVersion = Discord4J.VERSION.toLowerCase().replace("-snapshot", "").split("\\.");
		} catch (NumberFormatException e) {
			Discord4J.LOGGER.error("Module {} has incorrect minimum Discord4J version syntax! ({})", module.getName(), module.getMinimumDiscord4JVersion());
			return false;
		}
		for (int i = 0; i < Math.min(versions.length, 3); i++) {
			if (!(Integer.parseInt(versions[i]) <= Integer.parseInt(discord4jVersion[i])))
				return false;
		}
		return true;
	}

	/**
	 * Loads a jar file and automatically adds any modules.
	 *
	 * @param file The jar file to load.
	 */
	public static synchronized void loadExternalModules(File file) { //A bit hacky, but oracle be dumb and encapsulates URLClassLoader#addUrl()
		if (file.isFile() && file.getName().endsWith(".jar")) { //Can't be a directory and must be a jar
			try {
				//Executes would should be URLCLassLoader.addUrl(file.toURI().toURL());
				URLClassLoader loader = (URLClassLoader) ClassLoader.getSystemClassLoader();
				URL url = file.toURI().toURL();
				for (URL it : Arrays.asList(loader.getURLs())) {//Ensures duplicate libraries aren't loaded
					if (it.equals(url)) {
						return;
					}
				}
				Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
				method.setAccessible(true);
				method.invoke(loader, new Object[]{url});

				//Scans the jar file for classes which have IModule as a super class
				List<String> classes = new ArrayList<>();
				try (JarFile jar = new JarFile(file)) {
					jar.stream().forEach(jarEntry -> {
						if (!jarEntry.isDirectory() && jarEntry.getName().endsWith(".class")) {
							String className = jarEntry.getName().replace('/', '.');
							classes.add(className.substring(0, className.length()-".class".length()));
						}
					});
				}
				for (String clazz : classes) {
					Class classInstance = Class.forName(clazz);
					if (IModule.class.isAssignableFrom(classInstance)) {
						addModuleClass(classInstance);
					}
				}
			} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | IOException | ClassNotFoundException e) {
				Discord4J.LOGGER.error("Unable to load module "+file.getName()+"!", e);
			}
		}
	}

	/**
	 * This loads a list of jar files and automatically resolves any dependency issues.
	 *
	 * @param files The jar files to load.
	 */
	public static void loadExternalModules(List<File> files) {
		List<File> independents = new ArrayList<>();
		List<File> dependents = new ArrayList<>();

		files.forEach((file) -> {
			try {
				JarFile jarFile = new JarFile(file);
				Manifest manifest = jarFile.getManifest();
				if (manifest != null && manifest.getMainAttributes() != null
						&& manifest.getMainAttributes().containsKey("module-requires")) {
					dependents.add(file);
				} else {
					independents.add(file);
				}
			} catch (IOException e) {
				Discord4J.LOGGER.error("Discord4J Internal Exception");
			}
		});

		independents.forEach(ModuleLoader::loadExternalModules);

		List<File> noLongerDependents = dependents.stream().filter(jarFile -> { //loads all dependents whose requirements have been met already
			try {
				Class clazz = Class.forName(new JarFile(jarFile).getManifest().getMainAttributes().getValue("module-requires"));
				return clazz != null;
			} catch (Exception e) {
				return false;
			}
		}).collect(Collectors.toList());
		dependents.removeAll(noLongerDependents);
		noLongerDependents.forEach(ModuleLoader::loadExternalModules);

		dependents.removeIf((file -> { //Filters out all unusable files
			boolean cannotBeLoaded = true;
			try {
				cannotBeLoaded = findFileForClass(dependents,
						new JarFile(file).getManifest().getMainAttributes().getValue("module-requires")) == null;
			} catch (IOException ignored) {}

			if (cannotBeLoaded)
				Discord4J.LOGGER.warn("Unable to load module file {}. Its dependencies cannot be resolved!", file.getName());

			return cannotBeLoaded;
		}));

		dependents.forEach(ModuleLoader::loadExternalModules);
	}

	private static File findFileForClass(List<File> files, String clazz) {
		return files.stream().filter((file) -> {
			try {
				JarFile jarFile = new JarFile(file);
				return jarFile.getJarEntry(clazz.replaceAll("\\.", File.pathSeparator)+".class") != null;
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
		modules.add(clazz);
	}
}
