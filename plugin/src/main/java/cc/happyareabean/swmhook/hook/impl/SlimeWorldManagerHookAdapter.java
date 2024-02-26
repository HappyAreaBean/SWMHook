package cc.happyareabean.swmhook.hook.impl;

import cc.happyareabean.swmhook.event.SWMWorldLoadedEvent;
import cc.happyareabean.swmhook.hook.HookAdapter;
import com.grinderwolf.swm.api.SlimePlugin;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import com.grinderwolf.swm.api.world.SlimeWorld;
import com.grinderwolf.swm.plugin.SWMPlugin;
import com.grinderwolf.swm.plugin.config.ConfigManager;
import com.grinderwolf.swm.plugin.config.WorldData;
import com.grinderwolf.swm.plugin.config.WorldsConfig;
import com.grinderwolf.swm.plugin.log.Logging;
import lombok.extern.log4j.Log4j2;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;

import java.io.IOException;

@Log4j2
public class SlimeWorldManagerHookAdapter extends HookAdapter {

    private final SlimePlugin plugin = (SlimePlugin) Bukkit.getPluginManager().getPlugin("SlimeWorldManager");

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
        return ((SWMPlugin) plugin).getNms().getSlimeWorld(world) != null;
    }

    @Override
    public void loadWorld(String templateWorldName, String worldName, String loaderName) {
        WorldsConfig config = ConfigManager.getWorldConfig();
        WorldData worldData = config.getWorlds().get(templateWorldName);
        SlimeLoader loader = getLoader(loaderName);

        try {
            long start = System.currentTimeMillis();

            if (loader == null) {
                throw new IllegalArgumentException("invalid data source " + worldData.getDataSource());
            }

            SlimeWorld slimeWorld = plugin.loadWorld(loader, templateWorldName, true, worldData.toPropertyMap()).clone(worldName);
            Bukkit.getScheduler().runTask(SWMPlugin.getInstance(), () -> {
                try {
                    SWMPlugin.getInstance().generateWorld(slimeWorld);
                } catch (IllegalArgumentException ex) {
                    log.info(Logging.COMMAND_PREFIX + ChatColor.RED + "Failed to generate world " + worldName + ": " + ex.getMessage() + ".");
                    return;
                }

                log.info(Logging.COMMAND_PREFIX + ChatColor.GREEN + "World " + ChatColor.YELLOW + worldName
                        + ChatColor.GREEN + " loaded and generated in " + (System.currentTimeMillis() - start) + "ms!");
                Bukkit.getPluginManager().callEvent(new SWMWorldLoadedEvent(templateWorldName, worldName, true));
            });
        } catch (Throwable ex) {
            log.error(Logging.COMMAND_PREFIX + ChatColor.RED + "Failed to generate world " + worldName + ": " + ex.getMessage() + ".", ex);
        }
    }

    @Override
    public String getPluginName() {
        return "SlimeWorldManager";
    }

    @Override
    public String getProviderAuthor() {
        return "HappyAreaBean";
    }

    private SlimeLoader getLoader(String loader) {
        return plugin.getLoader(loader);
    }
}
