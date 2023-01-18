package cc.happyareabean.swmhook.commands;

import cc.happyareabean.swmhook.constants.Constants;
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

	@Subcommand({"worldlist", "wl"})
	@Description("List current world")
	public void worldList(BukkitCommandActor actor, @Optional(def = "false") boolean slimeOnly, @Optional String filter) {

		int totalWorlds = 0;

		List<Component> list = new ArrayList<>();
		if (filter != null) {
			list.add(text("Searching for: ", NamedTextColor.RED).append(text(filter, NamedTextColor.YELLOW)).append(text("...")));
			if (Bukkit.getWorlds().stream().noneMatch(w -> w.getName().contains(filter))) {
				list.add(space());
				list.add(text("No worlds match your search criteria.", NamedTextColor.RED));
			}
		}
		list.add(space());

		List<Component> worldList = new ArrayList<>();
		Bukkit.getWorlds().stream().filter(w -> {
			if (slimeOnly) {
				return SWMPlugin.getInstance().getNms().getSlimeWorld(w) != null;
			}
			return true;
		}).filter(w -> filter == null || w.getName().contains(filter)).sorted(Comparator.comparing(World::getName)).forEachOrdered(w -> {
			TextComponent.Builder c = text();
			c.append(text(" - ", NamedTextColor.GRAY));
			c.append(text(w.getName(), NamedTextColor.GREEN)
					.hoverEvent(text("Click here to teleport to world ", NamedTextColor.RED, TextDecoration.BOLD).append(text(w.getName(), NamedTextColor.GOLD)))
					.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/swmhook tp " + w.getName())));

			if (!slimeOnly) {
				c.append(SWMPlugin.getInstance().getNms().getSlimeWorld(w) != null ? text(" [SLIME]", NamedTextColor.DARK_GREEN) : empty());
			} else {
				c.append(text(" [SLIME]", NamedTextColor.DARK_GREEN));
			}

			worldList.add(c.build());
		});

		totalWorlds = worldList.size();
		list.addAll(worldList);

		List<Component> finalList = new ArrayList<>();
		finalList.add(text("----------------------------------------").color(NamedTextColor.DARK_GRAY).decorate(TextDecoration.STRIKETHROUGH));
		finalList.add(LEGACY_SERIALIZER.deserialize(String.format("&c&lSWMHook World Search &7- &fTotal of &9%s &fworlds", totalWorlds)));
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
