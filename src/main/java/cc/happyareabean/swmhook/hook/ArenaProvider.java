package cc.happyareabean.swmhook.hook;

import cc.happyareabean.swmhook.SWMHook;
import cc.happyareabean.swmhook.objects.SWMHWorld;

public abstract class ArenaProvider {

	/**
	 * Add arena with provider
	 * @param world A SWMHWorld object
	 */
	public void addArena(SWMHWorld world) {

	}

	/**
	 * Remove arena from provider
	 * @param world A SWMHWorld object
	 */
	public void removeArena(SWMHWorld world) {

	}

	public String getProviderName()	{
		return null;
	}

	public void log(String message) {
		SWMHook.log(String.format("[%s] %s", getProviderName(), message));
	}

	public void info(String message) {
		SWMHook.info(String.format("[%s] %s", getProviderName(), message));
	}

	public void success(String message) {
		SWMHook.success(String.format("[%s] %s", getProviderName(), message));
	}

}
