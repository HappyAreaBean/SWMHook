package cc.happyareabean.swmhook.hook.impl;

import cc.happyareabean.swmhook.hook.ArenaProvider;
import cc.happyareabean.swmhook.objects.SWMHWorld;

public class DefaultArenaProvider extends ArenaProvider {

	@Override
	public void addArena(SWMHWorld world) {
		super.addArena(world);
	}

	@Override
	public String getProviderName() {
		return "Default";
	}
}
