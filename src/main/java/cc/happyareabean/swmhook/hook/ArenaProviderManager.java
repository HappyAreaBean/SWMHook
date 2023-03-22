package cc.happyareabean.swmhook.hook;

import cc.happyareabean.swmhook.SWMHook;
import cc.happyareabean.swmhook.hook.impl.DefaultArenaProvider;
import cc.happyareabean.swmhook.hook.impl.EdenArenaProvider;
import cc.happyareabean.swmhook.objects.SWMHWorld;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.semver4j.Semver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ArenaProviderManager {

	@Getter
	private ArenaProvider provider;
	@Getter
	private List<SWMHWorld> loadFailed = new ArrayList<>();

	public ArenaProviderManager() {
		PluginManager pm = Bukkit.getPluginManager();

		if (pm.getPlugin("Eden") != null) {
			this.provider = new EdenArenaProvider();
		}

		// If provider assigned, verify required version
		if (provider != null) {
			providerPluginVersionCheck();
			return;
		}

		// If provider not assigned, set to default provider
		this.provider = new DefaultArenaProvider();
	}

	public void providerPluginVersionCheck() {
		if (provider.getRequiredPluginVersion() == null) return;
		String version = Bukkit.getPluginManager().getPlugin(provider.getProviderName()).getDescription().getVersion();
		Semver semver = new Semver(version.contains("-") ? version.split("-")[0] : version);

		if (!semver.isGreaterThanOrEqualTo(semver)) {
			provider.log("The provider is incompatible to your current version.");
			provider.info(String.format("Required: %s | Your version: %s", provider.getRequiredPluginVersion(), semver.getVersion()));
			this.provider = new DefaultArenaProvider();
		}
	}

	public String getProviderName() {
		return this.provider.getProviderName();
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
}
