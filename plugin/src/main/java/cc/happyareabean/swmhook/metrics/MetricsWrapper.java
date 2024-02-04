package cc.happyareabean.swmhook.metrics;

import cc.happyareabean.swmhook.SWMHook;
import cc.happyareabean.swmhook.arenaprovider.ArenaProvider;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.DrilldownPie;
import org.bstats.charts.SingleLineChart;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class MetricsWrapper {

	private final SWMHook swmhook;
	private final Metrics metrics;

	public MetricsWrapper(JavaPlugin plugin, int serviceId) {
		this.swmhook = (SWMHook) Bukkit.getPluginManager().getPlugin("SWMHook");
		this.metrics = new Metrics(plugin, serviceId);

		addArenaProviderChart();
		addWorldCountChart();
	}

	public void addWorldCountChart() {
		metrics.addCustomChart(new SingleLineChart("swmhWorldCount", () -> swmhook.getWorldsList().getWorlds().size()));
	}

	public void addArenaProviderChart() {
		metrics.addCustomChart(new DrilldownPie("swmhArenaProvider", () -> {
			final Map<String, Map<String, Integer>> result = new HashMap<>();
			final Map<String, Integer> backend = new HashMap<>();
			final ArenaProvider arenaProvider = swmhook.getArenaProviderManager().getProvider();

			backend.put(arenaProvider.getProviderName(), 1);
			result.put(arenaProvider.getProviderName(), backend);
			return result;
		}));
	}
}
