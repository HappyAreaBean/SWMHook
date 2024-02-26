package cc.happyareabean.swmhook.hook.impl;

import cc.happyareabean.swmhook.hook.HookAdapter;
import org.bukkit.World;

public class DefaultHookAdapter extends HookAdapter {

	@Override
	public boolean isLoaderValid(String loader) {
		return false;
	}

	@Override
	public boolean isWorldExist(String worldName, String loader) {
		return false;
	}

	@Override
	public boolean isSlimeWorld(World world) {
		return false;
	}

	@Override
	public void loadWorld(String templateWorldName, String worldName, String loaderName) {

	}

	@Override
	public String getPluginName() {
		return "Default";
	}

	@Override
	public String getProviderAuthor() {
		return "HappyAreaBean";
	}

	@Override
	public String getAdapterName() {
		return "Default";
	}
}
