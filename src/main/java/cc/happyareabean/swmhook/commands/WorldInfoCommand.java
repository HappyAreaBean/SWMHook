package cc.happyareabean.swmhook.commands;

import cc.happyareabean.swmhook.SWMHook;
import cc.happyareabean.swmhook.objects.SWMHWorld;
import cc.happyareabean.swmhook.utils.Color;
import com.grinderwolf.swm.plugin.SWMPlugin;
import org.bukkit.World;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Description;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.ArrayList;
import java.util.List;

@Command({"worldinfo"})
@CommandPermission("swmhook.admin")
public class WorldInfoCommand {

	@DefaultFor({"worldinfo"})
	@Description("Check your current world and info")
	public void world(BukkitCommandActor actor, @Optional Player target) {
		if (actor.isConsole()) {
			actor.error("As console, please specific a player name.");
			actor.errorLocalized("missing-argument", "target");
			return;
		}

		Player player = target == null ? actor.getAsPlayer() : target;
		World world = player.getWorld();
		SWMHWorld swmhWorld = SWMHook.getInstance().getWorldsList().getFromWorld(world);

		List<String> list = new ArrayList<>();
		list.add("&8&m----------------------------------------");
		list.add(String.format("World name: &b%s", world.getName()));
		list.add(String.format("Is it in slime format: &a%s", Color.toColorBoolean(SWMPlugin.getInstance().getNms().getSlimeWorld(world) != null)));
		list.add(String.format("Is it in SWMHook World Template: &a%s", Color.toColorBoolean(swmhWorld != null)));
		list.add(String.format("In Arena Provider: &a%s", swmhWorld == null ? Color.toColorBoolean(false) :
				String.format("&a%s &7- &e%s",SWMHook.getInstance().getArenaProviderManager().getProviderName(), swmhWorld.getTemplateName())));
		list.add("&8&m----------------------------------------");

		list.forEach(actor::reply);
	}

}
