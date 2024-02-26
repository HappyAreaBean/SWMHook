package cc.happyareabean.swmhook.arenaprovider.listener;

import cc.happyareabean.swmhook.SWMHook;
import cc.happyareabean.swmhook.event.SWMWorldLoadedEvent;
import cc.happyareabean.swmhook.objects.SWMHWorld;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ArenaProviderListener implements Listener {

    private final SWMHook plugin = SWMHook.getInstance();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onWorldLoaded(SWMWorldLoadedEvent e) {
        World world = Bukkit.getWorld(e.getWorldName());
        SWMHWorld swmhWorld = plugin.getWorldsList().getFromName(e.getTemplateName());

        if (swmhWorld == null) return;
        if (world == null) return;

        switch (swmhWorld.getType()) {
            case STATIC -> plugin.getArenaProviderManager().getProvider().addArena(swmhWorld);
            case ON_DEMAND -> plugin.getArenaProviderManager().getProvider().loadArena(swmhWorld.getTemplateName(), world);
        }
    }

}
