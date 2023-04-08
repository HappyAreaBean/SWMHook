package cc.happyareabean.swmhook.hook;

import cc.happyareabean.swmhook.SWMHook;
import cc.happyareabean.swmhook.hook.impl.DefaultArenaProvider;
import cc.happyareabean.swmhook.hook.impl.EdenArenaProvider;
import cc.happyareabean.swmhook.objects.SWMHWorld;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.semver4j.Semver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class ArenaProviderManager {

	@Getter
	private ArenaProvider provider;
	@Getter
	private List<SWMHWorld> loadFailed = new ArrayList<>();
	private static final String PROVIDERS_FOLDER = "providers";

	public ArenaProviderManager(JavaPlugin plugin) {
		PluginManager pm = Bukkit.getPluginManager();

		if (loadArenaProviders(plugin, PROVIDERS_FOLDER)) {
			this.provider.onInitialization();
			if (!this.provider.isCanRegister()) {
				SWMHook.log("====================================================================");
				SWMHook.log(String.format("The provider %s can't be register!", provider.getProviderName()));
				SWMHook.log("Please check console to see if there any relevant errors or contact the author.");
				SWMHook.log("====================================================================");
				SWMHook.log("Provider name: " + provider.getProviderName());
				SWMHook.log("Provider version: " + provider.getProviderVersion());
				SWMHook.log("Provider author: " + provider.getProviderAuthor());
				SWMHook.log("====================================================================");
				fallbackToDefault();
				return;
			}
			providerPluginVersionCheck();
			return;
		}

		if (pm.getPlugin("Eden") != null) {
			this.provider = new EdenArenaProvider();
		}

		// If provider assigned, verify required version
		if (provider != null) {
			providerPluginVersionCheck();
			return;
		}

		// If provider not assigned, set to default provider
		fallbackToDefault();
	}

	public void providerPluginVersionCheck() {
		if (provider.getProviderVersion() == null) {
			provider.log("Provider doesn't provide a version! Fall back to the default provider.");
			fallbackToDefault();
			return;
		}
		if (provider.getRequiredPluginVersion() == null) return;
		String version = Bukkit.getPluginManager().getPlugin(provider.getProviderName()).getDescription().getVersion();
		Semver semver = new Semver(version.contains("-") ? version.split("-")[0] : version);

		if (!semver.isGreaterThanOrEqualTo(semver)) {
			provider.log("The provider is incompatible to your current version.");
			provider.info(String.format("Required: %s | Your version: %s", provider.getRequiredPluginVersion(), semver.getVersion()));
			fallbackToDefault();
		}
	}

	public String getProviderName() {
		return this.provider.getProviderName();
	}

	public void fallbackToDefault() {
		this.provider = new DefaultArenaProvider();
	}

	public static void errorWhenAdding(ArenaProvider provider, SWMHWorld world, String message) {
		SWMHook.log(String.format("Error when adding arena from template world %s to provider %s: %s",
				world.getTemplateName(), provider.getProviderName(), message));
	}

	public void addFailedWorld(SWMHWorld world) {
		loadFailed.add(world);
	}

	public void checkFailedWorld() {
		if (loadFailed.size() == 0)
			return;

		Arrays.asList(
				"",
				String.format("&cThe following %s SWMHWorld failed to add as an arena in arena provider %s:", loadFailed.size(), getProviderName()),
				loadFailed.stream().map(w -> " &c- " + w.getTemplateName()).collect(Collectors.joining("\n")),
				"",
				"&cPlease make sure that SWMHWorld is configured properly:",
				String.format(" &c- The SWMHWorld template world is an arena in your provider %s.", getProviderName()),
				String.format(" &c- Make sure your template world names are the same in the %s's %s.", getProviderName(), provider.getProviderFileName()),
				" &c- The arena setup is complete.",
				""
		).forEach(SWMHook::prefixedLog);

		loadFailed.clear();
	}

	public void setProvider(ArenaProvider provider) {
		this.provider = provider;
		SWMHook.log("[ArenaProvider] Provider has been changed to: " + provider.getProviderName());
	}

	private boolean loadArenaProviders(JavaPlugin plugin, String folder) {
		File modulesFolder = new File(plugin.getDataFolder(), folder);
		if (!modulesFolder.exists()) {
			modulesFolder.mkdir();
		}

		if (!modulesFolder.isDirectory()) {
			return false;
		}

		File[] moduleJars = modulesFolder.listFiles((dir, name) -> name.endsWith(".jar"));
		if (moduleJars == null) {
			return false;
		}

		for (File moduleJar : moduleJars) {
			try (JarFile jar = new JarFile(moduleJar)) {
				Enumeration<JarEntry> entries = jar.entries();
				while (entries.hasMoreElements()) {
					JarEntry entry = entries.nextElement();
					if (entry.getName().endsWith(".class")) {
						String className = entry.getName().replace("/", ".").replace(".class", "");
						try {
							Class<?> clazz = Class.forName(className, true, new ProviderClassLoader(modulesFolder));
							if (ArenaProvider.class.isAssignableFrom(clazz)) {
								ArenaProvider provider = (ArenaProvider) clazz.newInstance();
								this.setProvider(provider);
								jar.close();
								return true;
							}
						} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
							e.printStackTrace();
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	private static class ProviderClassLoader extends URLClassLoader {

		private ProviderClassLoader(File moduleDirectory) {
			super(new URL[0], ProviderClassLoader.class.getClassLoader());
			if (!moduleDirectory.exists() || !moduleDirectory.isDirectory()) {
				return;
			}
			for (File moduleFile : Objects.requireNonNull(moduleDirectory.listFiles((dir, name) -> name.endsWith(".jar")))) {
				try {
					addURL(moduleFile.toURI().toURL());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		protected Class<?> findClass(String name) throws ClassNotFoundException {
			try {
				return super.findClass(name);
			} catch (ClassNotFoundException e) {
				return getParent().loadClass(name);
			}
		}

		@Override
		public InputStream getResourceAsStream(String name) {
			InputStream stream = super.getResourceAsStream(name);
			return stream != null ? stream : getParent().getResourceAsStream(name);
		}

	}
}
