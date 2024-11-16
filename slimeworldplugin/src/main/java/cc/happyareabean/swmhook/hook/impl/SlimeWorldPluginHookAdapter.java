package cc.happyareabean.swmhook.hook.impl;

import cc.happyareabean.swmhook.event.SWMWorldLoadedEvent;
import cc.happyareabean.swmhook.hook.HookAdapter;
import com.infernalsuite.aswm.api.AdvancedSlimePaperAPI;
import com.infernalsuite.aswm.api.exceptions.CorruptedWorldException;
import com.infernalsuite.aswm.api.exceptions.NewerFormatException;
import com.infernalsuite.aswm.api.exceptions.UnknownWorldException;
import com.infernalsuite.aswm.api.loaders.SlimeLoader;
import com.infernalsuite.aswm.api.world.SlimeWorld;
import com.infernalsuite.aswm.api.world.properties.SlimeProperties;
import com.infernalsuite.aswm.api.world.properties.SlimePropertyMap;
import com.infernalsuite.aswm.plugin.SWPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class SlimeWorldPluginHookAdapter extends HookAdapter {

    private final Logger log = LogManager.getLogger();
    private final SWPlugin plugin = (SWPlugin) Bukkit.getPluginManager().getPlugin("SlimeWorldPlugin");
    private final AdvancedSlimePaperAPI asp = AdvancedSlimePaperAPI.instance();

    @Override
    public boolean isLoaderValid(String loader) {
        return plugin.getLoaderManager().getLoader(loader) != null;
    }

    @Override
    public boolean isWorldExist(String worldName, String loader) {
        try {
            return plugin.getLoaderManager().getLoader(loader).worldExists(worldName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean isSlimeWorld(World world) {
        return asp.getLoadedWorld(world.getName()) != null;
    }

    @Override
    public void loadWorld(String templateWorldName, String worldName, String loaderName) {
        SlimeLoader loader = plugin.getLoaderManager().getLoader(loaderName);

        try {
            long start = System.currentTimeMillis();

            if (loader == null) {
                log.error("Invalid data source " + loaderName);
            }

            CompletableFuture.runAsync(() -> {
                try {
                    SlimeWorld world = asp.getLoadedWorld(templateWorldName) == null ?
                            asp.readWorld(loader, templateWorldName, false, createPropertyMap(0, 100, 0)) :
                            asp.getLoadedWorld(templateWorldName);
                    SlimeWorld slimeWorld = world.clone(worldName);

                    Bukkit.getScheduler().runTask(plugin, () -> {
                        asp.loadWorld(slimeWorld, true);

                        log.info(ChatColor.GREEN + "World " + ChatColor.YELLOW + worldName + ChatColor.GREEN + " loaded and generated in " + (System.currentTimeMillis() - start) + "ms!");
                        Bukkit.getPluginManager().callEvent(new SWMWorldLoadedEvent(templateWorldName, worldName, true));
                    });
                } catch (UnknownWorldException | CorruptedWorldException | NewerFormatException | IOException ex) {
                    log.error(ChatColor.RED + "Failed to generate world " + worldName + ": " + ex.getMessage() + ".", ex);
                }
            });
        } catch (Throwable ex) {
            log.error(ChatColor.RED + "Failed to generate world " + worldName + ": " + ex.getMessage() + ".", ex);
        }
    }

    @Override
    public String getAdapterName() {
        return "Slime World Plugin (SWP)";
    }

    @Override
    public String getPluginName() {
        return "SlimeWorldPlugin";
    }

    @Override
    public String getProviderAuthor() {
        return "HappyAreaBean";
    }

    private SlimePropertyMap createPropertyMap(int spawnX, int spawnY, int spawnZ) {
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
