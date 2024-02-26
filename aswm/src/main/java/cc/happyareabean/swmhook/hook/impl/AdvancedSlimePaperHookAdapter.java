package cc.happyareabean.swmhook.hook.impl;

import cc.happyareabean.swmhook.event.SWMWorldLoadedEvent;
import cc.happyareabean.swmhook.hook.HookAdapter;
import com.infernalsuite.aswm.api.SlimePlugin;
import com.infernalsuite.aswm.api.loaders.SlimeLoader;
import com.infernalsuite.aswm.api.world.SlimeWorld;
import com.infernalsuite.aswm.api.world.properties.SlimeProperties;
import com.infernalsuite.aswm.api.world.properties.SlimePropertyMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class AdvancedSlimePaperHookAdapter extends HookAdapter {

    private final Logger log = LogManager.getLogger();
    private final SlimePlugin plugin = (SlimePlugin) Bukkit.getPluginManager().getPlugin("SlimeWorldManager");

    @Override
    public boolean isLoaderValid(String loader) {
        return plugin.getLoader(loader) != null;
    }

    @Override
    public boolean isWorldExist(String worldName, String loader) {
        try {
            return plugin.getLoader(loader).worldExists(worldName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean isSlimeWorld(World world) {
        return plugin.getWorld(world.getName()) != null;
    }

    @Override
    public void loadWorld(String templateWorldName, String worldName, String loaderName) {
        SlimeLoader loader = plugin.getLoader(loaderName);

        try {
            long start = System.currentTimeMillis();

            if (loader == null) {
                log.error("Invalid data source " + loaderName);
            }

            SlimeWorld slimeWorld = plugin.getWorld(templateWorldName);
            if (slimeWorld == null) {
                slimeWorld = plugin.loadWorld(
                        loader,
                        templateWorldName,
                        true,
                        createPropertyMap(0, 100, 0)
                );
            }

            SlimeWorld finalWorld = slimeWorld.clone(worldName);

            plugin.loadWorld(finalWorld);

            log.info(ChatColor.GREEN + "World " + ChatColor.YELLOW + worldName + ChatColor.GREEN + " loaded and generated in " + (System.currentTimeMillis() - start) + "ms!");
            Bukkit.getScheduler().runTask((Plugin) plugin, () -> Bukkit.getPluginManager().callEvent(new SWMWorldLoadedEvent(templateWorldName, worldName, true)));
        } catch (Throwable ex) {
            log.error(ChatColor.RED + "Failed to generate world " + worldName + ": " + ex.getMessage() + ".", ex);
        }
    }

    @Override
    public String getAdapterName() {
        return "AdvancedSlimeWorldManager";
    }

    @Override
    public String getPluginName() {
        return "SlimeWorldManager";
    }

    @Override
    public String getProviderAuthor() {
        return "HappyAreaBean";
    }

    private @NotNull SlimePropertyMap createPropertyMap(int spawnX, int spawnY, int spawnZ) {
        SlimePropertyMap propertyMap = new SlimePropertyMap();
        propertyMap.setValue(SlimeProperties.WORLD_TYPE, "flat");
        propertyMap.setValue(SlimeProperties.SPAWN_X, spawnX);
        propertyMap.setValue(SlimeProperties.SPAWN_Y, spawnY);
        propertyMap.setValue(SlimeProperties.SPAWN_Z, spawnZ);
        propertyMap.setValue(SlimeProperties.ALLOW_ANIMALS, false);
        propertyMap.setValue(SlimeProperties.ALLOW_MONSTERS, false);
        propertyMap.setValue(SlimeProperties.DIFFICULTY, "easy");
        propertyMap.setValue(SlimeProperties.PVP, true);
        return propertyMap;
    }
}
