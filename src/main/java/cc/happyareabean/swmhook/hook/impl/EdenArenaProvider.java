package cc.happyareabean.swmhook.hook.impl;

import cc.happyareabean.swmhook.SWMHook;
import cc.happyareabean.swmhook.hook.ArenaProvider;
import cc.happyareabean.swmhook.hook.ArenaProviderManager;
import cc.happyareabean.swmhook.objects.SWMHWorld;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import rip.diamond.practice.arenas.Arena;
import rip.diamond.practice.arenas.ArenaDetail;

import java.util.List;
import java.util.stream.Collectors;

public class EdenArenaProvider extends ArenaProvider {

	@Override
	public void addArena(SWMHWorld world) {

		List<Arena> arenas = Arena.getArenas().stream().filter(a -> !a.isEnabled()).collect(Collectors.toList());

		if (arenas.stream().noneMatch(a -> a.getName().equalsIgnoreCase(world.getTemplateName()))) {
			log(world.getTemplateName(), "No available template in Eden's arenas.yml, please make sure you use template world created a arena first!");
			log(world.getTemplateName(), "Please ALSO remember the arena MUST be disabled in Eden!");
			return;
		}

		arenas.stream().filter(a -> a.getName().equalsIgnoreCase(world.getTemplateName())).findFirst().ifPresent(arena -> {

			// Check if the arena is finished setup
			if (!arena.isFinishedSetup()) {
				ArenaProviderManager.errorWhenAdding(this, world, "Arena find but setup is not finished. Aborting...");
				return;
			}

			for (int i = 0; i < world.getAmount(); i++) {
				int currentNumber = i + 1;
				String toBeGenerated = world.getWorldName() + currentNumber;
				info(String.format("Adding arena details: [%s] from template world [%s] to provider %s...", toBeGenerated, world.getTemplateName(), getProviderName()));

				ArenaDetail generatedArenaDetail = newArenaDetail(arena, toBeGenerated);
				generatedArenaDetail.copyChunk();
				arena.getArenaDetails().add(generatedArenaDetail);
				arena.setEnabled(true);

				success(String.format("Added arena details [%s].", toBeGenerated));
			}
		});
	}

	@Override
	public void removeArena(SWMHWorld world) {
		Arena arena = Arena.getArena(world.getTemplateName());

		if (arena == null) return;

		info(String.format("Restore arena details and state: [%s] in provider %s...", world.getTemplateName(), getProviderName()));
		arena.setArenaDetails(Lists.newArrayList(arena.getArenaDetails().get(0)));
		arena.setEnabled(false);
		success(String.format("Restored arena details and state: [%s] from provider %s!", world.getTemplateName(), getProviderName()));
	}

	@Override
	public boolean isArena(World world) {
		SWMHWorld swmhWorld = SWMHook.getInstance().getWorldsList().getFromWorld(world);

		if (swmhWorld == null) return false;
		if (Arena.getArena(swmhWorld.getTemplateName()) == null) return false;

		Arena arena = Arena.getArena(swmhWorld.getTemplateName());

		return arena.getArenaDetails().stream().anyMatch(a -> a.getA().getWorld().getName().equalsIgnoreCase(world.getName()));
	}

	@Override
	public String getProviderName() {
		return "Eden";
	}

	private ArenaDetail newArenaDetail(Arena arena, String worldName) {
		Location a = arena.getA().clone();
		Location b = arena.getB().clone();
		Location min = arena.getMin().clone();
		Location max = arena.getMax().clone();

		a.setWorld(Bukkit.getWorld(worldName));
		b.setWorld(Bukkit.getWorld(worldName));
		min.setWorld(Bukkit.getWorld(worldName));
		max.setWorld(Bukkit.getWorld(worldName));

		return new ArenaDetail(arena, a, b, min, max);
	}
}
