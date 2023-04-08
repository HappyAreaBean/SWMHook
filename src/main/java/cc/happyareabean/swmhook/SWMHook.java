package cc.happyareabean.swmhook;

import cc.happyareabean.swmhook.commands.ProviderInfoCommand;
import cc.happyareabean.swmhook.commands.SWMHookCommand;
import cc.happyareabean.swmhook.commands.WorldInfoCommand;
import cc.happyareabean.swmhook.config.SWMHWorldsList;
import cc.happyareabean.swmhook.constants.Constants;
import cc.happyareabean.swmhook.event.SWMWorldLoadedEvent;
import cc.happyareabean.swmhook.hook.ArenaProvider;
import cc.happyareabean.swmhook.hook.ArenaProviderManager;
import cc.happyareabean.swmhook.metrics.MetricsWrapper;
import cc.happyareabean.swmhook.objects.SWMHWorld;
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
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import revxrsal.commands.bukkit.BukkitCommandHandler;
import revxrsal.commands.exception.CommandErrorException;

import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;

@Getter
public class SWMHook extends JavaPlugin {

	@Getter private SWMHWorldsList worldsList;
	@Getter private ArenaProviderManager arenaProviderManager;
	@Getter private static SWMHook instance;
	@Getter private MetricsWrapper metricsWrapper;

	@Override
	public void onEnable() {
		instance = this;
		startupMessage();

		worldsList = new SWMHWorldsList(new File(this.getDataFolder(), "worlds.yml").toPath());

		if (worldsList.getWorlds().size() == 1) {
			if (worldsList.getWorlds().get(0).getTemplateName().equalsIgnoreCase("default")) {
				log("====================================================================");
				log("Look like this is your first time running SWMHook.");
				log("SWMHook will not load anything until you configure your 'worlds.yml' properly!");
				log("You can get started by editing 'worlds.yml' or using /swmhook add to add a world!");
				log("====================================================================");
			}
		}

		loadAllSWMHWorld();
		arenaProviderManager = new ArenaProviderManager(this);
		info("Current arena provider: " + arenaProviderManager.getProviderName());

		new BukkitRunnable() {
			@Override
			public void run() {
				addToArena();
			}
		}.runTaskLater(this, 1);

		info("Loading commands...");
		BukkitCommandHandler commandHandler = BukkitCommandHandler.create(this);
		commandHandler.getAutoCompleter().registerParameterSuggestions(World.class, (args, sender, command) -> Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList()));
		commandHandler.getAutoCompleter().registerParameterSuggestions(SWMHWorld.class, (args, sender, command) -> worldsList.getWorlds().stream().map(SWMHWorld::getTemplateName).collect(Collectors.toList()));
		commandHandler.registerValueResolver(SWMHWorld.class, context -> {
			String value = context.pop();
			if (worldsList.getWorlds().stream().noneMatch(w -> w.getTemplateName().equalsIgnoreCase(value))) {
				throw new CommandErrorException("Invalid SWMHWorld: &e" + value);
			}
			return worldsList.getWorlds().stream().filter(w -> w.getTemplateName().equalsIgnoreCase(value)).findFirst().orElse(null);
		});
		commandHandler.setHelpWriter((command, actor) -> String.format(" &8• &e/%s %s &7- &f%s", command.getPath().toRealString(), command.getUsage(), command.getDescription()));
		commandHandler.register(new SWMHookCommand(), new WorldInfoCommand(), new ProviderInfoCommand());
		commandHandler.enableAdventure();

		metricsWrapper = new MetricsWrapper(this, 17549);

		info("SWMHook by HappyAreaBean has been enabled!");
	}

	@Override
	public void onDisable() {
		unLoadAllSWMHWorld();
		prefixedLog("Thank you and good bye!");
	}

	public void addToArena() {
		worldsList.getWorlds().forEach(w -> arenaProviderManager.getProvider().addArena(w));
		arenaProviderManager.checkFailedWorld();
	}

	public void loadAllSWMHWorld() {
		SlimePlugin slimePlugin = (SlimePlugin) Bukkit.getPluginManager().getPlugin("SlimeWorldManager");

		worldsList.getWorlds().forEach(world -> {

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
		if (worldsList.getWorlds().get(0).getTemplateName().equalsIgnoreCase("default")) return;
		worldsList.getWorlds().forEach(world -> {

			if (world.getAmount() == 0) return;

			if (arenaProviderManager.getProvider().isArena(Bukkit.getWorld(world.getTemplateName())))
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

	public void startupMessage() {
		prefixedLog("");
		Constants.LOGO_LIST.forEach(SWMHook::prefixedLog);
		prefixedLog("");
		prefixedLog(" &2" + getDescription().getDescription());
		prefixedLog(" &fv" + Constants.VERSION + " &fMade With &4❤ &fBy HappyAreaBean");
		prefixedLog("");
	}

	/**
	 * This method will override the default arena provider when launching.
	 * Useful if you are creating your own arena provider.
	 * @param provider A arena provider
	 */
	public void setArenaProvider(ArenaProvider provider) {
		arenaProviderManager.setProvider(provider);
		log("[SET] Arena provider has been changed to: " + provider.getProviderName());
	}

	public static void prefixedLog(String message) {
		Bukkit.getConsoleSender().sendMessage(Color.translate(Constants.PREFIX + message));
	}

	public static void prefixedLog() {
		Bukkit.getConsoleSender().sendMessage(Color.translate(Constants.PREFIX));
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
