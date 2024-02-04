package cc.happyareabean.swmhook.commands;

import cc.happyareabean.swmhook.SWMHook;
import cc.happyareabean.swmhook.arenaprovider.ArenaProvider;
import cc.happyareabean.swmhook.hook.HookProvider;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Description;
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
		HookProvider hook = SWMHook.getInstance().getHookProviderManager().getHook();

		List<String> list = new ArrayList<>();
		list.add("&8&m----------------------------------------");
		list.add(" &b&lArena Provider");
		list.add(String.format("   Name: &a%s", provider.getProviderName()));
		list.add(String.format("   Version: &a%s", provider.getProviderVersion()));
		list.add(String.format("   Author: &a%s", provider.getProviderAuthor()));
		list.add(String.format("   Required plugin version: &a%s", provider.getRequiredPluginVersion() == null ? "None" : provider.getRequiredPluginVersion()));
		list.add(" &b&lHook Provider");
		list.add(String.format("   Name: &a%s", hook.getProviderName()));
		list.add(String.format("   Plugin: &a%s", hook.getPluginName()));
		list.add(String.format("   Author: &a%s", hook.getProviderAuthor()));
		list.add("&8&m----------------------------------------");

		list.forEach(actor::reply);
	}

}
