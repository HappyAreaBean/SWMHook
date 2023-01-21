package cc.happyareabean.swmhook.commands;

import cc.happyareabean.swmhook.SWMHook;
import cc.happyareabean.swmhook.config.SWMHWorldsList;
import cc.happyareabean.swmhook.constants.Constants;
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
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Description;
import revxrsal.commands.annotation.Named;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;
import revxrsal.commands.help.CommandHelp;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static cc.happyareabean.swmhook.utils.Utils.LEGACY_SERIALIZER;
import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;

@SuppressWarnings("unused")
@Command({"swmhook", "swmh"})
@CommandPermission("swmhook.admin")
public class SWMHookCommand {

	@DefaultFor({"swmhook", "swmh"})
	@Description("SWMHook commands list")
	public void help(BukkitCommandActor actor, CommandHelp<String> helpEntries, @Optional(def = "1") int page) {
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
	public void remove(BukkitCommandActor actor, SWMHWorld swmhWorld) {
		if (SWMHook.getInstance().getWorldsList().getWorlds().remove(swmhWorld)) {
			SWMHook.getInstance().getWorldsList().save();
			actor.reply(String.format("&aRemoved world &f%s &cin SWMHook!", swmhWorld.toFancyString()));
			actor.reply(Constants.RELOAD_WORLD);
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

	@Subcommand("amount")
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
						.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/swmhook loadprovider"))));
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

	@Subcommand({"worldlist", "wl"})
	@Description("List current world")
	public void worldList(BukkitCommandActor actor, @Optional(def = "ALL") SWMWorldSearchType type, @Optional String filter) {

		int totalWorlds = 0;

		List<Component> list = new ArrayList<>();
		if (filter != null) {
			list.add(text("Searching for: ", NamedTextColor.WHITE, TextDecoration.BOLD).append(text(filter, NamedTextColor.AQUA)).append(text("...")));
			if (Bukkit.getWorlds().stream().noneMatch(w -> w.getName().contains(filter))) {
				list.add(space());
				list.add(text("No worlds match your search criteria.", NamedTextColor.RED));
			}
		}

		List<Component> worldList = new ArrayList<>();
		Bukkit.getWorlds().stream().filter(w -> {
			switch (type) {
				case NORMAL:
					return SWMPlugin.getInstance().getNms().getSlimeWorld(w) == null;
				case SLIME_ONLY:
					return SWMPlugin.getInstance().getNms().getSlimeWorld(w) != null;
				case PROVIDER:
					return SWMHook.getInstance().getArenaProviderManager().getProvider().isArena(w);
				case ALL:
				default:
					return true;
			}
		}).filter(w -> filter == null || w.getName().contains(filter)).sorted(Comparator.comparing(World::getName)).forEachOrdered(w -> {
			TextComponent.Builder c = text();
			c.append(text("   - ", NamedTextColor.GRAY));
			c.append(text(w.getName(), NamedTextColor.GREEN)
					.hoverEvent(text("Click here to teleport to world ", NamedTextColor.RED, TextDecoration.BOLD).append(text(w.getName(), NamedTextColor.GOLD)))
					.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/swmhook tp " + w.getName())));

			if (type != SWMWorldSearchType.SLIME_ONLY) {
				c.append(SWMPlugin.getInstance().getNms().getSlimeWorld(w) != null ? text(" [SLIME]", NamedTextColor.DARK_GREEN) : empty());
			} else {
				c.append(text(" [SLIME]", NamedTextColor.DARK_GREEN));
			}

			ArenaProvider arenaProvider = SWMHook.getInstance().getArenaProviderManager().getProvider();
			if (type != SWMWorldSearchType.PROVIDER) {
				if (arenaProvider.isArena(w)) {
					c.append(text(String.format(" [%s]", arenaProvider.getProviderName().toUpperCase()), NamedTextColor.DARK_RED));
				}
			} else {
				c.append(text(String.format(" [%s]", arenaProvider.getProviderName().toUpperCase()), NamedTextColor.DARK_RED));
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

	public static List<String> buildCommandHelp(CommandHelp<String> helpEntries, int page, String subCommand) {
		if (subCommand != null) helpEntries.removeIf(s -> !s.contains(subCommand));
		int slotPerPage = 10;
		int maxPages = helpEntries.getPageSize(slotPerPage);
		List<String> list = new ArrayList<>();
		list.add("&8&m----------------------------------------");
		list.add(String.format("&c&lSWMHook &f(v%s) &7- &fPage &9(%s/%s)", Constants.VERSION, page, maxPages));
		list.add("&fMade With &4❤ &fBy HappyAreaBean");
		list.add("");
		list.addAll(helpEntries.paginate(page, slotPerPage));
		list.add("");
		list.add("&8&m----------------------------------------");
		return list;
	}

}
