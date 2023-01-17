package cc.happyareabean.swmhook.hook;

import cc.happyareabean.swmhook.SWMHook;
import cc.happyareabean.swmhook.hook.impl.DefaultArenaProvider;
import cc.happyareabean.swmhook.hook.impl.EdenArenaProvider;
import cc.happyareabean.swmhook.objects.SWMHWorld;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

public class ArenaProviderManager {

	@Getter
	private ArenaProvider provider;

	public ArenaProviderManager() {
		PluginManager pm = Bukkit.getPluginManager();

		if (pm.getPlugin("Eden") != null) {
			this.provider = new EdenArenaProvider();
			return;
		}

		this.provider = new DefaultArenaProvider();
	}

	public String getProviderName() {
		return this.provider.getProviderName();
	}

	public static void errorWhenAdding(ArenaProvider provider, SWMHWorld world, String message) {
		SWMHook.log(String.format("Error when adding arena from template world %s to provider %s: %s",
				world.getTemplateName(), provider.getProviderName(), message));
	}
}
