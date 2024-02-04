package cc.happyareabean.swmhook.hook.impl;

import cc.happyareabean.swmhook.hook.HookProvider;

public class DefaultHookProvider extends HookProvider {

	@Override
	public boolean isLoaderValid(String loader) {
		return false;
	}

	@Override
	public boolean isWorldExist(String worldName, String loader) {
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
	public String getProviderName() {
		return "Default";
	}
}
