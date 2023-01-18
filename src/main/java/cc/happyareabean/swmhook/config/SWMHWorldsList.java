package cc.happyareabean.swmhook.config;

import cc.happyareabean.swmhook.objects.SWMHWorld;
import cc.happyareabean.swmhook.objects.SWMLoaderType;
import de.exlll.configlib.annotation.ElementType;
import de.exlll.configlib.configs.yaml.YamlConfiguration;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.World;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

@Getter @Setter
public class SWMHWorldsList extends YamlConfiguration {

	@ElementType(SWMHWorld.class)
	private List<SWMHWorld> worlds = Collections.singletonList(SWMHWorld.builder()
			.loader(SWMLoaderType.FILE)
			.templateName("default")
			.worldName("default")
			.amount(5)
			.build());

	public SWMHWorldsList(Path path) {
		super(path);
		this.loadAndSave();
	}

	public SWMHWorld getFromWorld(World world) {
		if (worlds.stream().noneMatch(w -> world.getName().contains(w.getWorldName()))) return null;

		return worlds.stream().filter(w -> world.getName().contains(w.getWorldName()))
				.findFirst().orElse(null);
	}
}
