package cc.happyareabean.swmhook;

import cc.happyareabean.swmhook.arenaprovider.ArenaProvider;
import cc.happyareabean.swmhook.arenaprovider.ArenaProviderManager;
import cc.happyareabean.swmhook.arenaprovider.listener.ArenaProviderListener;
import cc.happyareabean.swmhook.commands.ProviderInfoCommand;
import cc.happyareabean.swmhook.commands.SWMHookCommand;
import cc.happyareabean.swmhook.commands.WorldInfoCommand;
import cc.happyareabean.swmhook.config.SWMHWorldsList;
import cc.happyareabean.swmhook.constants.Constants;
import cc.happyareabean.swmhook.hook.HookAdapter;
import cc.happyareabean.swmhook.hook.HookAdapterManager;
import cc.happyareabean.swmhook.metrics.MetricsWrapper;
import cc.happyareabean.swmhook.objects.SWMHWorld;
import cc.happyareabean.swmhook.objects.SWMWorldType;
import cc.happyareabean.swmhook.utils.Color;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import revxrsal.commands.bukkit.BukkitCommandHandler;
import revxrsal.commands.exception.CommandErrorException;

import java.io.File;
import java.util.UUID;
import java.util.stream.Collectors;

import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;

@Getter
public class SWMHook extends JavaPlugin {

	@Getter private SWMHWorldsList worldsList;
	@Getter private ArenaProviderManager arenaProviderManager;
	@Getter private HookAdapterManager hookAdapterManager;
	@Getter private static SWMHook instance;
	@Getter private MetricsWrapper metricsWrapper;
	private boolean firstTime = false;

	@Override
	public void onEnable() {
		instance = this;
		startupMessage();

		worldsList = new SWMHWorldsList(new File(this.getDataFolder(), "worlds.yml").toPath());

		if (worldsList.getWorlds().size() == 1) {
			if (worldsList.getWorlds().get(0).getTemplateName().equalsIgnoreCase(Constants.DEFAULT_WORLDS_STRING)) {
				log("====================================================================");
				log("Look like this is your first time running SWMHook.");
				log("SWMHook will not load anything until you configure your 'worlds.yml' properly!");
				log("You can get started by editing 'worlds.yml' or using /swmhook add to add a world!");
				log("====================================================================");
				firstTime = true;
			}
		}

		hookAdapterManager = new HookAdapterManager(this);
		info("Current hook provider: " + hookAdapterManager.getProviderName());

		arenaProviderManager = new ArenaProviderManager(this);
		info("Current arena provider: " + arenaProviderManager.getProviderName());

		Bukkit.getPluginManager().registerEvents(new ArenaProviderListener(), this);

		if (arenaProviderManager.getProviderName().equals("Default") || hookAdapterManager.getProviderName().equals("Default")) {
			if (!firstTime) {
				log("====================================================================");
				log("Look like SWMHook are using default arena or hook provider. Are there something went wrong?");
				log("");
				log("Possible fixes:");
				log("- Check your provider jar is put in the right folder");
				log("====================================================================");
			}
		} else {
			// This need to run after the server are fully booted up
			// Because the ArenaProvider won't be able to register the arena.
			new BukkitRunnable() {
				@Override
				public void run() {
					loadAllSWMHWorld();
				}
			}.runTaskLater(this, 1L);
		}

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
		commandHandler.setHelpWriter((command, actor) -> {
			return Component.textOfChildren(
					space(),
					text("•", NamedTextColor.DARK_GRAY),
					space(),
					text(String.format("/%s %s", command.getPath().toRealString(), command.getUsage()))
			).hoverEvent(HoverEvent.showText(text(command.getDescription() == null ?
					"This command does not have a description." : command.getDescription(), NamedTextColor.GREEN)));
		});
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

	public void loadAllSWMHWorld() {
		worldsList.getWorlds().forEach(world -> {
			HookAdapter hook = hookAdapterManager.getHook();
			String loaderName = world.getLoader().name().toLowerCase();
			String templateName = world.getTemplateName();

//			if (hook.isLoaderValid(loaderName)) {
//				log(String.format("The loader [%s] for template world [%s] are invalid, skipped loading.", world.getLoader(), world.getTemplateName()));
//				return;
//			}
//
//			if (!hook.isWorldExist(templateName, loaderName)) return;

			if (world.getType() == SWMWorldType.STATIC && world.getAmount() == 0) return;

			int amount = world.getType() == SWMWorldType.STATIC ? world.getAmount() : world.getMin();

			for (int i = 0; i < amount; i++) {
				int currentNumber = i + 1;
				String toBeGenerated = world.getWorldName() + (world.getType() == SWMWorldType.STATIC ? currentNumber : "_" + UUID.randomUUID());
				info(String.format("Loading world: [%s] from [%s]...", toBeGenerated, world.getTemplateName()));
				hook.loadWorld(templateName, toBeGenerated, loaderName);
			}

		});
	}

	/**
	 * <strong>ON DEMAND USES ONLY</strong>
	 * @param world a SWMHWorld object
	 */
	public void generateWorld(SWMHWorld world) {
		HookAdapter hook = hookAdapterManager.getHook();
		String loaderName = world.getLoader().name().toLowerCase();
		String templateName = world.getTemplateName();

		String toBeGenerated = world.getWorldName() + "_" + UUID.randomUUID();
		info(String.format("Loading world: [%s] from [%s]...", toBeGenerated, world.getTemplateName()));
		hook.loadWorld(templateName, toBeGenerated, loaderName);
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
					log(ChatColor.GREEN + "World " + ChatColor.YELLOW + toBeUnload + ChatColor.GREEN + " unloaded correctly.");
				} else {
					log(ChatColor.RED + "Failed to unload world " + toBeUnload + ".");
				}
			}

		});
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
