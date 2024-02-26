package cc.happyareabean.swmhook.arenaprovider.impl;

import cc.happyareabean.swmhook.arenaprovider.ArenaProvider;
import cc.happyareabean.swmhook.constants.Constants;
import cc.happyareabean.swmhook.objects.SWMHWorld;

public class DefaultArenaProvider extends ArenaProvider {

	@Override
	public void addArena(SWMHWorld swmhWorld) {
		super.addArena(swmhWorld);
	}

	@Override
	public String getProviderName() {
		return "Default";
	}

	@Override
	public String getProviderAuthor() {
		return "HappyAreaBean";
	}

	@Override
	public String getProviderVersion() {
		return Constants.VERSION;
	}
}
