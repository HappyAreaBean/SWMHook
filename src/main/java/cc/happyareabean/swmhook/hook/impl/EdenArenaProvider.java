package cc.happyareabean.swmhook.hook.impl;

import cc.happyareabean.swmhook.hook.ArenaProvider;
import cc.happyareabean.swmhook.hook.ArenaProviderManager;
import cc.happyareabean.swmhook.objects.SWMHWorld;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import rip.diamond.practice.arenas.Arena;
import rip.diamond.practice.arenas.ArenaDetail;

import java.util.List;
import java.util.stream.Collectors;

public class EdenArenaProvider extends ArenaProvider {

	@Override
	public void addArena(SWMHWorld world) {

		List<Arena> arenas = Arena.getArenas().stream().filter(a -> !a.isEnabled()).collect(Collectors.toList());

		if (arenas.stream().noneMatch(a -> a.getName().equalsIgnoreCase(world.getTemplateName()))) {
			log("No available template in Eden's arenas.yml, please make sure you use template world created a arena first!");
			log("Please ALSO remember the arena MUST be disabled in Eden!");
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
		info(String.format("Restore arena details and state: [%s] in provider %s...", world.getTemplateName(), getProviderName()));
		Arena arena = Arena.getArena(world.getTemplateName());
		arena.setArenaDetails(Lists.newArrayList(arena.getArenaDetails().get(0)));
		arena.setEnabled(false);
		success(String.format("Restored arena details and state: [%s] from provider %s!", world.getTemplateName(), getProviderName()));
	}

	@Override
	public String getProviderName() {
		return "Eden";
	}

	private ArenaDetail newArenaDetail(Arena arena, String worldName) {
		Location a = arena.getA();
		Location b = arena.getB();
		Location min = arena.getMin();
		Location max = arena.getMax();

		a.setWorld(Bukkit.getWorld(worldName));
		b.setWorld(Bukkit.getWorld(worldName));
		min.setWorld(Bukkit.getWorld(worldName));
		max.setWorld(Bukkit.getWorld(worldName));

		return new ArenaDetail(arena, a, b, min, max);
	}
}