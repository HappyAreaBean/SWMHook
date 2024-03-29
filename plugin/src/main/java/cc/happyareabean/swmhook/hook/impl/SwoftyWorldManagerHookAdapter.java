package cc.happyareabean.swmhook.hook.impl;

import cc.happyareabean.swmhook.SWMHook;
import cc.happyareabean.swmhook.event.SWMWorldLoadedEvent;
import cc.happyareabean.swmhook.hook.HookAdapter;
import lombok.extern.log4j.Log4j2;
import net.swofty.swm.api.SlimePlugin;
import net.swofty.swm.api.loaders.SlimeLoader;
import net.swofty.swm.api.world.SlimeWorld;
import net.swofty.swm.api.world.data.WorldData;
import net.swofty.swm.api.world.data.WorldsConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;

import java.io.IOException;

@Log4j2
public class SwoftyWorldManagerHookAdapter extends HookAdapter {

    private final SlimePlugin plugin = (SlimePlugin) Bukkit.getPluginManager().getPlugin("SwoftyWorldManager");

    @Override
    public boolean isLoaderValid(String loader) {
        return plugin.getLoader(loader) != null;
    }

    @Override
    public boolean isWorldExist(String worldName, String loader) {
        try {
            return getLoader(loader).worldExists(worldName);
        } catch (IOException ex) {
            return false;
        }
    }

    @Override
    public boolean isSlimeWorld(World world) {
        return plugin.getSlimeWorlds().containsKey(world.getName());
    }

    @Override
    public void loadWorld(String templateWorldName, String worldName, String loaderName) {
        WorldsConfig config = plugin.getConfigManager().getWorldConfig();
        WorldData worldData = config.getWorlds().get(templateWorldName);
        SlimeLoader loader = getLoader(loaderName);

        try {
            long start = System.currentTimeMillis();

            if (loader == null) {
                throw new IllegalArgumentException("invalid data source " + worldData.getDataSource());
            }

            SlimeWorld slimeWorld = plugin.loadWorld(loader, templateWorldName, true, worldData.toPropertyMap()).clone(worldName);
            Bukkit.getScheduler().runTask(SWMHook.getInstance(), () -> {
                try {
                    plugin.generateWorld(slimeWorld);
                } catch (IllegalArgumentException ex) {
                    log.info(ChatColor.RED + "Failed to generate world " + worldName + ": " + ex.getMessage() + ".");
                    return;
                }

                log.info(ChatColor.GREEN + "World " + ChatColor.YELLOW + worldName
                        + ChatColor.GREEN + " loaded and generated in " + (System.currentTimeMillis() - start) + "ms!");
                Bukkit.getPluginManager().callEvent(new SWMWorldLoadedEvent(templateWorldName, worldName, true));
            });
        } catch (Throwable ex) {
            log.error(ChatColor.RED + "Failed to generate world " + worldName + ": " + ex.getMessage() + ".", ex);
        }
    }

    @Override
    public String getAdapterName() {
        return "Continued Slime World Manager";
    }

    @Override
    public String getPluginName() {
        return "SwoftyWorldManager";
    }

    @Override
    public String getProviderAuthor() {
        return "HappyAreaBean";
    }

    private SlimeLoader getLoader(String loader) {
        return plugin.getLoader(loader);
    }
}
