package cc.happyareabean.swmhook.hook;

import cc.happyareabean.swmhook.SWMHook;
import cc.happyareabean.swmhook.hook.impl.AdvancedSlimePaperHookProvider;
import cc.happyareabean.swmhook.hook.impl.DefaultHookProvider;
import cc.happyareabean.swmhook.hook.impl.SlimeWorldManagerHookProvider;
import cc.happyareabean.swmhook.hook.impl.SwoftyWorldManagerHookProvider;
import cc.happyareabean.swmhook.objects.SWMHWorld;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

@Getter
public class HookProviderManager {

	private HookProvider hook;

	public HookProviderManager(JavaPlugin plugin) {
		PluginManager pm = Bukkit.getPluginManager();

		if (pm.isPluginEnabled("SlimeWorldManager")) {
			try {
				Class.forName("com.infernalsuite.aswm.InternalPlugin");
				register(new AdvancedSlimePaperHookProvider());
				return;
			} catch (ClassNotFoundException ignored) {
				register(new SlimeWorldManagerHookProvider());
				return;
			}
		}

		if (pm.isPluginEnabled("SwoftyWorldManager")) {
			register(new SwoftyWorldManagerHookProvider());
			return;
		}

		// If provider not assigned, set to default provider
		fallbackToDefault();
	}

	public void register(HookProvider provider) {
		Objects.requireNonNull(provider.getPluginName(), "The plugin name is null!");
		Objects.requireNonNull(provider.getProviderAuthor(), "The provider author is null!");

		this.hook = provider;

		if (!this.hook.isCanRegister()) {
			SWMHook.log("====================================================================");
			SWMHook.log(String.format("The provider %s can't be register!", provider.getProviderName()));
			SWMHook.log("Please check console to see if there any relevant errors or contact the author.");
			SWMHook.log("====================================================================");
			SWMHook.log("Provider plugin name: " + provider.getPluginName());
			SWMHook.log("Provider name: " + provider.getProviderName());
			SWMHook.log("Provider author: " + provider.getProviderAuthor());
			SWMHook.log("====================================================================");
			fallbackToDefault();
        }
	}

	public String getProviderName() {
		return this.hook.getProviderName();
	}

	public void fallbackToDefault() {
		this.hook = new DefaultHookProvider();
	}

	public static void errorWhenAdding(HookProvider provider, SWMHWorld world, String message) {
		SWMHook.log(String.format("Error when adding arena from template world %s to provider %s: %s",
				world.getTemplateName(), provider.getProviderName(), message));
	}

	public void setHook(HookProvider hook) {
		this.hook = hook;
		SWMHook.log(String.format("[HookProvider] Provider has been changed to: %s by %s",
				hook.getProviderName(), hook.getProviderAuthor()));
	}

}
