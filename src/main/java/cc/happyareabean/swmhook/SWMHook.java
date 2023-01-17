package cc.happyareabean.swmhook;

import cc.happyareabean.swmhook.config.SWMHWorldsList;
import cc.happyareabean.swmhook.event.SWMWorldLoadedEvent;
import cc.happyareabean.swmhook.hook.ArenaProviderManager;
import cc.happyareabean.swmhook.utils.Color;
import com.grinderwolf.swm.api.SlimePlugin;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import com.grinderwolf.swm.api.world.SlimeWorld;
import com.grinderwolf.swm.plugin.SWMPlugin;
import com.grinderwolf.swm.plugin.config.ConfigManager;
import com.grinderwolf.swm.plugin.config.WorldData;
import com.grinderwolf.swm.plugin.config.WorldsConfig;
import com.grinderwolf.swm.plugin.log.Logging;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;

@Getter
public class SWMHook extends JavaPlugin {

	@Getter private SWMHWorldsList worlds;
	@Getter private ArenaProviderManager arenaProviderManager;

	@Override
	public void onEnable() {

		worlds = new SWMHWorldsList(new File(this.getDataFolder(), "worlds.yml").toPath());

		if (worlds.getWorlds().size() == 1) {
			if (worlds.getWorlds().get(0).getTemplateName().equalsIgnoreCase("default")) {
				log("This look like is your first time running SWMHook");
				log("SWMHook will not load anything until you configure your worlds.yml properly!");
				Bukkit.getPluginManager().disablePlugin(this);
				return;
			}
		}

		loadAllSWMHWorld();
		arenaProviderManager = new ArenaProviderManager();
		info("Current arena provider: " + arenaProviderManager.getProviderName());

		new BukkitRunnable() {
			@Override
			public void run() {
				worlds.getWorlds().forEach(w -> arenaProviderManager.getProvider().addArena(w));
			}
		}.runTaskLater(this, 1);

		info("SWMHook by HappyAreaBean has been enabled!");
	}

	@Override
	public void onDisable() {
		unLoadAllSWMHWorld();
		log("Thank you and good bye!");
	}

	public void loadAllSWMHWorld() {
		SlimePlugin slimePlugin = (SlimePlugin) Bukkit.getPluginManager().getPlugin("SlimeWorldManager");

		worlds.getWorlds().forEach(world -> {

			SlimeLoader loader = slimePlugin.getLoader(world.getLoader().name().toLowerCase());
			if (loader == null) {
				log(String.format("The loader [%s] for template world [%s] are invalid, skipped loading.", world.getLoader(), world.getTemplateName()));
				return;
			}

			try {
				if (!loader.worldExists(world.getTemplateName())) return;
			} catch (IOException e) {
				return;
			}

			if (world.getAmount() == 0) return;

			for (int i = 0; i < world.getAmount(); i++) {
				int currentNumber = i + 1;
				String toBeGenerated = world.getWorldName() + currentNumber;
				info(String.format("Loading world: [%s] from template world [%s]...", toBeGenerated, world.getTemplateName()));
				this.loadWorld(world.getTemplateName(), toBeGenerated, slimePlugin, loader);
			}

		});
	}

	public void unLoadAllSWMHWorld() {
		worlds.getWorlds().forEach(world -> {

			if (world.getAmount() == 0) return;

			arenaProviderManager.getProvider().removeArena(world);

			for (int i = 0; i < world.getAmount(); i++) {
				int currentNumber = i + 1;
				String toBeUnload = world.getWorldName() + currentNumber;
				info(String.format("Unloading world: [%s] from template world [%s]...", toBeUnload, world.getTemplateName()));
				if (Bukkit.unloadWorld(toBeUnload, false)) {
					log(Logging.COMMAND_PREFIX + ChatColor.GREEN + "World " + ChatColor.YELLOW + toBeUnload + ChatColor.GREEN + " unloaded correctly.");
				} else {
					log(Logging.COMMAND_PREFIX + ChatColor.RED + "Failed to unload world " + toBeUnload + ".");
				}
			}

		});
	}

	public void loadWorld(String templateWorldName, String worldName, SlimePlugin slimePlugin, SlimeLoader loader) {

		WorldsConfig config = ConfigManager.getWorldConfig();
		WorldData worldData = config.getWorlds().get(templateWorldName);

//		Bukkit.getScheduler().runTask(this, () -> {

			try {
				long start = System.currentTimeMillis();

				if (loader == null) {
					throw new IllegalArgumentException("invalid data source " + worldData.getDataSource());
				}

				SlimeWorld slimeWorld = slimePlugin.loadWorld(loader, templateWorldName, true, worldData.toPropertyMap()).clone(worldName);
				Bukkit.getScheduler().runTask(SWMPlugin.getInstance(), () -> {
					try {
						SWMPlugin.getInstance().generateWorld(slimeWorld);
					} catch (IllegalArgumentException ex) {
						log(Logging.COMMAND_PREFIX + ChatColor.RED + "Failed to generate world " + worldName + ": " + ex.getMessage() + ".");
						return;
					}

					info(Logging.COMMAND_PREFIX + ChatColor.GREEN + "World " + ChatColor.YELLOW + worldName
							+ ChatColor.GREEN + " loaded and generated in " + (System.currentTimeMillis() - start) + "ms!");
					Bukkit.getPluginManager().callEvent(new SWMWorldLoadedEvent(templateWorldName, worldName, true));
				});
			} catch (Throwable e) {
				log(Logging.COMMAND_PREFIX + ChatColor.RED + "Failed to generate world " + worldName + ": " + e.getMessage() + ".");
				e.printStackTrace();
			}
//		});
	}

	public static void log(String message) {
		Bukkit.getConsoleSender().sendMessage(Color.translate("&c[SWMHook] " + message));
	}

	public static void info(String message) {
		Bukkit.getConsoleSender().sendMessage(Color.translate("&e[SWMHook] " + message));
	}

	public static void success(String message) {
		Bukkit.getConsoleSender().sendMessage(Color.translate("&a[SWMHook] " + message));
	}
}
