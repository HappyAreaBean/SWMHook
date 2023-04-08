package cc.happyareabean.swmhook.commands;

import cc.happyareabean.swmhook.SWMHook;
import cc.happyareabean.swmhook.hook.ArenaProvider;
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

@Command({"providerinfo"})
@CommandPermission("swmhook.admin")
public class ProviderInfoCommand {

	@DefaultFor({"providerinfo"})
	@Description("Check your current provider nfo")
	public void providerinfo(BukkitCommandActor actor) {
		ArenaProvider provider = SWMHook.getInstance().getArenaProviderManager().getProvider();

		List<String> list = new ArrayList<>();
		list.add("&8&m----------------------------------------");
		list.add(String.format("Current provider name: &6&l%s", provider.getProviderName()));
		list.add(String.format("Provider version: &b&l%s", provider.getProviderVersion()));
		list.add(String.format("Required plugin version: &a&l%s", provider.getRequiredPluginVersion() == null ? "None" : provider.getRequiredPluginVersion()));
		list.add("&8&m----------------------------------------");

		list.forEach(actor::reply);
	}

}
