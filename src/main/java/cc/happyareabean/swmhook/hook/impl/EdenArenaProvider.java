package cc.happyareabean.swmhook.hook.impl;

import cc.happyareabean.swmhook.hook.ArenaProvider;
import cc.happyareabean.swmhook.hook.ArenaProviderManager;
import cc.happyareabean.swmhook.objects.SWMHWorld;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import rip.diamond.practice.arenas.Arena;
import rip.diamond.practice.arenas.ArenaDetail;
import rip.diamond.practice.arenas.task.ArenaRemoveTask;

import java.util.ArrayList;
import java.util.Collections;
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

			// Create a new list to save generated arena
			List<Arena> arenaList = new ArrayList<>();

			for (int i = 0; i < world.getAmount(); i++) {
				int currentNumber = i + 1;
				String toBeGenerated = world.getWorldName() + currentNumber;
				info(String.format("Adding arena: [%s] from template world [%s] to provider %s...", toBeGenerated, world.getTemplateName(), getProviderName()));

				Arena generatedArena = new Arena(toBeGenerated);
				generatedArena.setAllowedKits(arena.getAllowedKits());
				generatedArena.setIcon(arena.getIcon());
				generatedArena.setYLimit(arena.getYLimit());
				generatedArena.setBuildMax(arena.getBuildMax());
				generatedArena.setEnabled(true);
				ArenaDetail generatedArenaDetail = newArenaDetail(arena, generatedArena);
				generatedArenaDetail.copyChunk();
				generatedArena.getArenaDetails().add(generatedArenaDetail);

				arenaList.add(generatedArena);
				success(String.format("Added arena [%s] to temp arena list.", toBeGenerated));
			}

			info(String.format("Adding generated %s template arena to provider %s...", world.getTemplateName(), getProviderName()));

			Arena.getArenas().addAll(arenaList);

			success(String.format("Added %s generated %s template arena to provider %s!", arenaList.size(), world.getTemplateName(), getProviderName()));
		});
	}

	@Override
	public void removeArena(SWMHWorld world) {

		for (int i = 0; i < world.getAmount(); i++) {
			int currentNumber = i + 1;
			String toBeRemove = world.getWorldName() + currentNumber;
			info(String.format("Removing arena: [%s] from template world [%s] in provider %s...", toBeRemove, world.getTemplateName(), getProviderName()));
			Arena arena = Arena.getArena(toBeRemove);
			Arena.getArenas().remove(arena);
			success(String.format("Removed arena: [%s] from provider %s!", toBeRemove, getProviderName()));
		}
	}

	@Override
	public String getProviderName() {
		return "Eden";
	}

	private ArenaDetail newArenaDetail(Arena arena, Arena newArena) {
		Location a = arena.getA();
		Location b = arena.getB();
		Location min = arena.getMin();
		Location max = arena.getMax();

		a.setWorld(Bukkit.getWorld(newArena.getName()));
		b.setWorld(Bukkit.getWorld(newArena.getName()));
		min.setWorld(Bukkit.getWorld(newArena.getName()));
		max.setWorld(Bukkit.getWorld(newArena.getName()));

		return new ArenaDetail(newArena, a, b, min, max);
	}
}
