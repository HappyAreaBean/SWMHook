package cc.happyareabean.swmhook.hook;

import cc.happyareabean.swmhook.SWMHook;
import cc.happyareabean.swmhook.objects.SWMHWorld;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.World;

public abstract class ArenaProvider {

	/**
	 * This will determine whether can this provider can be register or not, default is true.
	 * <br><br>
	 * See also {@link #onInitialization()}, {@link #setCanRegister(boolean)} and {@link #isCanRegister()}
	 */
	@Getter(value = AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PROTECTED)
	public boolean canRegister = true;

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
	 * Check is the world is an arena in the provider
	 * @param world A {@link World} object
	 * @return <b>true</b> if world is an arena, else <b>false</b>
	 */
	public boolean isArena(World world) {
		return false;
	}

	/**
	 * This will be called before registering the provider.
	 * <br>
	 * You can perform some check before it registered to SWMHook.
	 * <br><br>
	 * See also {@link #setCanRegister(boolean)} and {@link #isCanRegister()}
	 */
	public void onInitialization() {

	}

	/**
	 * The name of this provider
	 *
	 * @return name of this provider
	 */
	public String getProviderName()	{
		return null;
	}

	/**
	 * The stored arenas file name of this provider
	 *
	 * @return the arenas file name of this provider
	 */
	public String getProviderFileName()	{
		return null;
	}

	/**
	 * The required plugin version for this provider to be registered
	 *
	 * @return the required plugin version for this provider
	 */
	public String getRequiredPluginVersion() {
		return null;
	}

	/**
	 * The version of this provider
	 *
	 * @return current version of this provider
	 */
	public String getProviderVersion() {
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
