package cc.happyareabean.swmhook.hook;

import cc.happyareabean.swmhook.SWMHook;
import cc.happyareabean.swmhook.objects.SWMHWorld;
import org.bukkit.World;

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

	/**
	 * check is the world is an arena in the provider
	 * @param world A {@link World} object
	 * @return <b>true</b> if world is an arena, else <b>false</b>
	 */
	public boolean isArena(World world) {
		return false;
	}

	public String getProviderName()	{
		return null;
	}

	public void log(String message) {
		SWMHook.log(String.format("[%s] %s", getProviderName(), message));
	}

	public void log(String arena, String message) {
		SWMHook.log(String.format("[%s] [%s] %s", getProviderName(), arena, message));
	}

	public void info(String message) {
		SWMHook.info(String.format("[%s] %s", getProviderName(), message));
	}

	public void success(String message) {
		SWMHook.success(String.format("[%s] %s", getProviderName(), message));
	}

}
