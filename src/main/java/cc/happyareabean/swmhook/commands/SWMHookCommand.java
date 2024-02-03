package cc.happyareabean.swmhook.commands;

import cc.happyareabean.swmhook.SWMHook;
import cc.happyareabean.swmhook.config.SWMHWorldsList;
import cc.happyareabean.swmhook.constants.Constants;
import cc.happyareabean.swmhook.constants.Tags;
import cc.happyareabean.swmhook.hook.ArenaProvider;
import cc.happyareabean.swmhook.objects.SWMHWorld;
import cc.happyareabean.swmhook.objects.SWMLoaderType;
import cc.happyareabean.swmhook.objects.SWMWorldSearchType;
import com.grinderwolf.swm.api.world.SlimeWorld;
import com.grinderwolf.swm.plugin.SWMPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Default;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Description;
import revxrsal.commands.annotation.Named;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.annotation.Switch;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;
import revxrsal.commands.help.CommandHelp;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static cc.happyareabean.swmhook.constants.Constants.HELP_COMMAND_FORMAT;
import static cc.happyareabean.swmhook.constants.Constants.PAGE_TEXT;
import static cc.happyareabean.swmhook.utils.Utils.LEGACY_SERIALIZER;
import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.textOfChildren;

@SuppressWarnings("unused")
@Command({"swmhook", "swmh"})
@CommandPermission("swmhook.admin")
public class SWMHookCommand {

	@DefaultFor({"swmhook", "swmh"})
	@Subcommand("help")
	@Description("SWMHook commands list")
	public void help(BukkitCommandActor actor, CommandHelp<TextComponent> helpEntries, @Optional @Default("1") int page) {
		buildCommandHelp(helpEntries, page, null).forEach(actor::reply);
	}

	@Subcommand({"worldinfo"})
	@Description("Check your current world and info")
	public void world(BukkitCommandActor actor, @Optional Player target) {
		new WorldInfoCommand().world(actor, target);
	}

	@Subcommand("tp")
	@Description("Teleport to a world")
	public void teleport(BukkitCommandActor actor, World world, @Optional Player target) {
		if (actor.isConsole()) {
			actor.error("As console, please specific a player name to teleport");
			actor.errorLocalized("missing-argument", "world", "target");
			return;
		}

		target = actor.getAsPlayer() != null ? actor.getAsPlayer() : target;

		actor.reply("Teleporting &e" + (target.getName().equals(actor.getSender().getName())
				? "yourself" : target.getName()) + " to world &b" + world.getName() + "&7...");

		Location spawnLocation = world.getSpawnLocation();

		// Safe Spawn Location
		while (spawnLocation.getBlock().getType() != Material.AIR || spawnLocation.getBlock().getRelative(BlockFace.UP).getType() != Material.AIR) {
			spawnLocation.add(0, 1, 0);
		}

		target.teleport(spawnLocation);
	}

	@Subcommand("add")
	@Description("Add a world to SWMHook")
	public void add(BukkitCommandActor actor, World world, @Named("worldName/-") String worldName, int amount, SWMLoaderType loaderType) {
		SlimeWorld slimeWorld = SWMPlugin.getInstance().getNms().getSlimeWorld(world);

		if (slimeWorld == null) {
			actor.error("The target world need to be a slime world!");
			return;
		}

		if (worldName.equals("-"))
			worldName = slimeWorld.getName();

		SWMHWorld swmhWorld = new SWMHWorld(loaderType, slimeWorld.getName(), worldName, amount);

		SWMHook.getInstance().getWorldsList().getWorlds().add(swmhWorld);
		SWMHook.getInstance().getWorldsList().save();
		actor.reply(String.format("&aAdded world &f%s &ato SWMHook!", swmhWorld.toFancyString()));
		actor.reply(Constants.RELOAD_WORLD);
	}

	@Subcommand("remove")
	@Description("Remove a world in SWMHook")
	public void remove(BukkitCommandActor actor, SWMHWorld swmhWorld, @Optional @Default("false") boolean showList) {
		if (SWMHook.getInstance().getWorldsList().getWorlds().remove(swmhWorld)) {
			SWMHook.getInstance().getWorldsList().save();
			actor.reply(String.format("&cRemoved world &f%s &cin SWMHook!", swmhWorld.toFancyString()));
			actor.reply(Constants.RELOAD_WORLD);

			if (showList)
				list(actor);
			return;
		}

		actor.reply(String.format("&cUnable to remove &f%s &cin SWMHook, check console for more information", swmhWorld.toFancyString()));
	}

	@Subcommand("disable")
	@Description("Disable SWMHook. Let you edit arenas from provider without saving any SWMH data")
	public void disable(BukkitCommandActor actor) {
		actor.reply("&eUnloading all SWMH worlds and remove arenas from provider...");
		SWMHook.getInstance().unLoadAllSWMHWorld();
		actor.reply("&aYou can always re-active SWMH by using &f/swmhook reloadWorlds&a!");
	}

	@DefaultFor({"swmhook edit", "swmh edit"})
	@Subcommand("edit help")
	@Description("SWMHook edit commands list")
	public void helpEdit(BukkitCommandActor actor, CommandHelp<TextComponent> helpEntries, @Optional @Default("1") int page) {
		buildCommandHelp(helpEntries, page, null).forEach(actor::reply);
	}

	@Subcommand("edit amount")
	@Description("Change world amount in SWMHook")
	public void amount(BukkitCommandActor actor, SWMHWorld swmhWorld, int amount) {
		SWMHWorldsList worldsList = SWMHook.getInstance().getWorldsList();
		int index = worldsList.getWorlds().indexOf(swmhWorld);
		int oldAmount = swmhWorld.getAmount();

		swmhWorld.setAmount(amount);
		worldsList.getWorlds().set(index, swmhWorld);
		worldsList.save();
		actor.reply(String.format("&aUpdated world &f%s &aamount from &f%s &ato &f%s&a!", swmhWorld.getTemplateName(), oldAmount, amount));
		actor.reply(Constants.RELOAD_WORLD);
	}

	@Subcommand("edit loader")
	@Description("Change SWMHook world loader type")
	public void loader(BukkitCommandActor actor, SWMHWorld swmhWorld, SWMLoaderType loader) {
		SWMHWorldsList worldsList = SWMHook.getInstance().getWorldsList();
		int index = worldsList.getWorlds().indexOf(swmhWorld);
		SWMLoaderType oldLoader = swmhWorld.getLoader();

		swmhWorld.setLoader(loader);
		worldsList.getWorlds().set(index, swmhWorld);
		worldsList.save();
		actor.reply(String.format("&aUpdated world &f%s &aloader from &f%s &ato &f%s&a!", swmhWorld.getTemplateName(), oldLoader, loader));
		actor.reply(Constants.RELOAD_WORLD);
	}

	@Subcommand({"swlist"})
	@Description("List all the slime world available in SWM's world config")
	public void swmList(BukkitCommandActor actor) {
		File swmFolder = new File(Bukkit.getServer().getUpdateFolderFile().getParentFile(), "SlimeWorldManager");
		YamlConfiguration configuration = YamlConfiguration.loadConfiguration(new File(swmFolder, "worlds.yml"));
        List<String> worldList = new ArrayList<>(configuration.getConfigurationSection("worlds").getKeys(false));

		int worldSize = worldList.size();
		List<Component> list = new ArrayList<>();
		list.add(LEGACY_SERIALIZER.deserialize("&8&m----------------------------------------"));
		list.add(LEGACY_SERIALIZER.deserialize(String.format("&c&lSlime World List &7- &fTotal of &9%s &fworld%s",
				worldSize, worldSize > 1 ? "s" : "")));
		if (worldSize > 0) {
			list.add(LEGACY_SERIALIZER.deserialize("&eClick the world name to load or teleport!"));
			list.add(LEGACY_SERIALIZER.deserialize("&cunloaded &7| &aloaded"));
			list.add(space());
			for (String world : worldList) {
				boolean loaded = Bukkit.getWorld(world) != null;
				list.add(textOfChildren(
						text("   - ", NamedTextColor.GRAY),
						text(String.format("%s", world), loaded ? NamedTextColor.GREEN : NamedTextColor.RED)
								.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND,
										loaded ? String.format("/swmh tp %s", world) : String.format("/swm load %s", world))))
								.hoverEvent(textOfChildren(
										newline(),
										text(loaded ? "Click to teleport to the world " : "Click to load the world ", NamedTextColor.RED, TextDecoration.BOLD)
												.append(text(world, NamedTextColor.GOLD, TextDecoration.BOLD))
								))
				);
			}
		} else {
			list.add(textOfChildren(
					text("World list are empty.", NamedTextColor.YELLOW)
			).decorate(TextDecoration.BOLD));
		}
		list.add(LEGACY_SERIALIZER.deserialize("&8&m----------------------------------------"));
		list.forEach(actor::reply);
	}

	@Subcommand("reloadWorlds")
	@Description("Reload SWMHook worlds")
	public void reloadWorlds(BukkitCommandActor actor) {
		SWMHook plugin = SWMHook.getInstance();
		long start = System.currentTimeMillis();
		actor.reply("&eUnloading all SWMH worlds...");
		plugin.unLoadAllSWMHWorld();
		actor.reply("&eLoad change from worlds.yml...");
		plugin.getWorldsList().loadAndSave();
		actor.reply("&eLoading all SWMH worlds...");
		plugin.loadAllSWMHWorld();
		long end = System.currentTimeMillis();
		actor.reply(String.format("&a&lDone! &eUsed &f%sms", (end - start)));
		actor.reply(text("TIP: ", NamedTextColor.GOLD, TextDecoration.BOLD)
				.append(text("You will need to reload provider using /swmhook loadprovider, or ", NamedTextColor.YELLOW))
				.append(text("click here", NamedTextColor.GREEN, TextDecoration.BOLD)
						.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/swmhook loadprovider")))
						.hoverEvent(text("Click here to load provider!", NamedTextColor.GREEN, TextDecoration.BOLD)));
		actor.reply(text("Please also make sure all the world are *loaded* before loading provider.", NamedTextColor.GRAY, TextDecoration.ITALIC));
		actor.reply(text("Recommend waiting 10 - 20 seconds after reloaded worlds.", NamedTextColor.GRAY, TextDecoration.ITALIC));
	}

	@Subcommand("loadProvider")
	@Description("Load SWMHook Arena Provider")
	public void reloadProvider(BukkitCommandActor actor) {
		SWMHook plugin = SWMHook.getInstance();
		long start = System.currentTimeMillis();
		actor.reply(String.format("&eAdding worlds to arena with provider &f%s...", plugin.getArenaProviderManager().getProviderName()));
		plugin.addToArena();
		long end = System.currentTimeMillis();
		actor.reply(String.format("&a&lDone! &eUsed &f%sms", (end - start)));
	}

	@Subcommand({"list"})
	@Description("List all the SWMHWorld in SWMHook")
	public void list(BukkitCommandActor actor) {
		List<SWMHWorld> worldList = SWMHook.getInstance().getWorldsList().getWorlds();
		int worldSize = worldList.size();
		List<Component> list = new ArrayList<>();
		list.add(LEGACY_SERIALIZER.deserialize("&8&m----------------------------------------"));
		list.add(LEGACY_SERIALIZER.deserialize(String.format("&c&lSWMHWorld List &7- &fTotal of &9%s &fworld%s",
				worldSize, worldSize > 1 ? "s" : "")));
		if (worldSize > 0) {
			list.add(LEGACY_SERIALIZER.deserialize("&eHover for more info!"));
			list.add(space());
			for (SWMHWorld world : worldList) {
				list.add(textOfChildren(
						text("[X]", NamedTextColor.RED, TextDecoration.BOLD)
								.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/swmhook remove %s true", world.getTemplateName())))
								.hoverEvent(text(String.format("Click here to delete %s", world.getTemplateName()),
										NamedTextColor.RED, TextDecoration.BOLD)),
						text("   - ", NamedTextColor.GRAY),
						text(String.format("%s", world.getTemplateName()), NamedTextColor.GREEN)
								.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/swmhook tp %s", world.getTemplateName())))
								.hoverEvent(textOfChildren(
										text("Loader: ", NamedTextColor.GRAY),
										text(world.getLoader().name(), NamedTextColor.WHITE),
										newline(),
										text("World Name: ", NamedTextColor.GRAY),
										text(world.getWorldName(), NamedTextColor.WHITE),
										newline(),
										text("Amount: ", NamedTextColor.GRAY),
										text(world.getAmount(), NamedTextColor.WHITE),
										newline(),
										newline(),
										text("Click to teleport to world ", NamedTextColor.RED, TextDecoration.BOLD)
												.append(text(world.getTemplateName(), NamedTextColor.GOLD, TextDecoration.BOLD))
								))
				));
			}
		} else {
			list.add(textOfChildren(
					text("SWHWorld list are empty.", NamedTextColor.YELLOW),
					space(),
					text("Click here to add some?", NamedTextColor.GOLD)
							.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/swmhook add "))
							.hoverEvent(text("Click here! :)", NamedTextColor.AQUA, TextDecoration.BOLD))
			).decorate(TextDecoration.BOLD));
		}
		list.add(LEGACY_SERIALIZER.deserialize("&8&m----------------------------------------"));
		list.forEach(actor::reply);
	}

	@Subcommand({"worldlist", "wl"})
	@Description("List current world")
	public void worldList(BukkitCommandActor actor,
						  @Optional @Default("ALL") SWMWorldSearchType type,
						  @Switch(value = "hideDup") boolean hideDuplicate,
						  @Optional String filter) {

		int totalWorlds = 0;

		List<Component> list = new ArrayList<>();
		if (filter != null) {
			list.add(text("Searching for: ", NamedTextColor.WHITE, TextDecoration.BOLD).append(text(filter, NamedTextColor.AQUA)).append(text("...")));
			if (Bukkit.getWorlds().stream().noneMatch(w -> w.getName().contains(filter))) {
				list.add(space());
				list.add(text("No worlds match your search criteria.", NamedTextColor.RED));
			}
		}

		if (hideDuplicate)
			list.add(text("Duplicated are hidden", NamedTextColor.GRAY, TextDecoration.ITALIC));

		List<Component> worldList = new ArrayList<>();
		Bukkit.getWorlds().stream().filter(w -> {
			switch (type) {
				case NORMAL:
					return SWMPlugin.getInstance().getNms().getSlimeWorld(w) == null;
				case SLIME_ONLY:
					return SWMPlugin.getInstance().getNms().getSlimeWorld(w) != null;
				case SWMH_ONLY:
					return SWMHook.getInstance().getWorldsList().getFromWorld(w) != null;
				case PROVIDER:
					return SWMHook.getInstance().getArenaProviderManager().getProvider().isArena(w);
				case ALL:
				default:
					return true;
			}
		}).filter(w -> filter == null || w.getName().contains(filter))
				.filter(w -> {
					if (!hideDuplicate) return true;
					SWMHWorld swmhWorld = SWMHook.getInstance().getWorldsList().getFromWorld(w);
					if (swmhWorld != null) {
						if (w.getName().equals(swmhWorld.getTemplateName()))
							return true;

						if (w.getName().contains(swmhWorld.getWorldName()))
							return false;
					}
					return true;
				}).sorted(Comparator.comparing(World::getName)).forEachOrdered(w -> {
			TextComponent.Builder c = text();
			c.append(text("   - ", NamedTextColor.GRAY));

			c.append(text(w.getName(), NamedTextColor.GREEN)
				.hoverEvent(text("Click here to teleport to world ", NamedTextColor.RED, TextDecoration.BOLD).append(text(w.getName(), NamedTextColor.GOLD)))
				.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/swmhook tp " + w.getName())));

			if (type != SWMWorldSearchType.SLIME_ONLY) {
				c.append(SWMPlugin.getInstance().getNms().getSlimeWorld(w) != null ? Tags.SLIME : empty());
			} else {
				c.append(Tags.SLIME);
			}

			if (type != SWMWorldSearchType.SWMH_ONLY) {
				c.append(SWMHook.getInstance().getWorldsList().getFromWorld(w) != null ? Tags.SWMH : empty());
			} else {
				c.append(Tags.SWMH);
			}

			ArenaProvider arenaProvider = SWMHook.getInstance().getArenaProviderManager().getProvider();
			if (type != SWMWorldSearchType.PROVIDER) {
				if (arenaProvider.isArena(w)) {
					c.append(Tags.fromProvider(arenaProvider.getProviderName()));
				}
			} else {
				c.append(Tags.fromProvider(arenaProvider.getProviderName()));
			}


			worldList.add(c.build());
		});

		totalWorlds = worldList.size();
		if (totalWorlds > 0) list.add(space());
		list.addAll(worldList);

		List<Component> finalList = new ArrayList<>();
		finalList.add(text("----------------------------------------").color(NamedTextColor.DARK_GRAY).decorate(TextDecoration.STRIKETHROUGH));
		finalList.add(LEGACY_SERIALIZER.deserialize(String.format("&c&lSWMHook World %s &7- &fTotal of &9%s &fworld%s &7- &6[%s]",
				filter != null ? "Search" : "List", totalWorlds, totalWorlds > 1 ? "s" : "", type)));
		finalList.addAll(list);
		finalList.add(text("----------------------------------------").color(NamedTextColor.DARK_GRAY).decorate(TextDecoration.STRIKETHROUGH));
		finalList.forEach(actor::reply);
	}

	public static List<Component> buildCommandHelp(CommandHelp<TextComponent> helpEntries, int page, String subCommand) {
		if (subCommand != null) helpEntries.removeIf(s -> !s.content().split("-")[0].contains(subCommand));
		int slotPerPage = 5;
		int maxPages = helpEntries.getPageSize(slotPerPage);
		List<Component> list = new ArrayList<>();
		list.add(LEGACY_SERIALIZER.deserialize("&8&m----------------------------------------"));
		list.add(LEGACY_SERIALIZER.deserialize(String.format("&c&lSWMHook &f(v%s) &7- &fPage &9(%s/%s)", Constants.VERSION, page, maxPages)));
		list.add(LEGACY_SERIALIZER.deserialize("&fMade With &4❤ &fBy HappyAreaBean"));
		list.add(LEGACY_SERIALIZER.deserialize("&eHover for more info!"));
		list.add(space());
		list.addAll(helpEntries.paginate(page, slotPerPage));
		list.add(space());
		if (maxPages > 1)
			list.add(paginateNavigation(page, maxPages, subCommand != null ? "/swmhook " + subCommand + "%s" : HELP_COMMAND_FORMAT));
		list.add(LEGACY_SERIALIZER.deserialize("&8&m----------------------------------------"));
		return list;
	}

	public static Component paginateNavigation(int currentPage, int maxPage, String commandFormat) {
		int previousPage = currentPage - 1;
		int nextPage = currentPage + 1;

		boolean havePreviousPage = previousPage != 0;
		boolean haveNextPage = maxPage != currentPage;

		TextComponent.Builder pageText = text()
				.decorate(TextDecoration.BOLD)
				.color(NamedTextColor.YELLOW);

		pageText.append(text("«", !havePreviousPage ? NamedTextColor.DARK_GRAY : null)
				.clickEvent(havePreviousPage ? ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, String.format(commandFormat, previousPage)) : null)
				.hoverEvent(havePreviousPage ? text(String.format(PAGE_TEXT, previousPage)).color(NamedTextColor.GOLD) : null));

		pageText.append(text(" ▍ "));

		pageText.append(text("»", !haveNextPage ? NamedTextColor.DARK_GRAY : null)
				.clickEvent(haveNextPage ? ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, String.format(commandFormat, nextPage)) : null)
				.hoverEvent(haveNextPage ? text(String.format(PAGE_TEXT, nextPage)).color(NamedTextColor.GOLD) : null));

		return pageText.build();
	}


}
