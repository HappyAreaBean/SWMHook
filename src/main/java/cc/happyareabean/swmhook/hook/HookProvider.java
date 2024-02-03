package cc.happyareabean.swmhook.hook;

import cc.happyareabean.swmhook.SWMHook;
import cc.happyareabean.swmhook.objects.SWMHWorld;
import lombok.Getter;
import lombok.Setter;

public abstract class HookProvider {

	/**
	 * This will determine whether can this provider can be register or not, default is true.
	 * <br><br>
	 * See also {@link #setCanRegister(boolean)} and {@link #isCanRegister()}
	 */
	@Getter @Setter
	public boolean canRegister = true;

	/**
	 * Check is the loader is a valid option in the HookProvider
	 * @param loader The loader name
	 * @return <b>true</b> if the loader is valid and found in the HookProvider, else <b>false</b>
	 */
	public abstract boolean isLoaderValid(String loader);

	/**
	 * Check is the world exist in the HookProvider
	 * @param worldName The world name
	 * @return <b>true</b> if the world name is existed in the HookProvider, else <b>false</b>
	 */
	public abstract boolean isWorldExist(String worldName, String loader);

	/**
	 * Load the world from the HookProvider.
	 * <br><br>
	 * This method will be used by SWMHook for loading any world.
	 * See also {@link SWMHWorld}
	 * @param templateWorldName The template world name to load the world from.
	 * @param worldName The world name to load the world to.
	 * @param loaderName The loader name to load the world.
	 */
	public abstract void loadWorld(String templateWorldName, String worldName, String loaderName);

	/**
	 * The name of this provider
	 *
	 * @return name of this provider
	 */
	public String getProviderName() {
		return getPluginName();
	}

	/**
	 * The required plugin name for this provider to be registered
	 *
	 * @return the required plugin name for this provider
	 */
	public abstract String getPluginName();

	/**
	 * The author of this provider
	 *
	 * @return the author of this provider
	 */
	public abstract String getProviderAuthor();

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
