package cc.happyareabean.swmhook.objects;

import cc.happyareabean.swmhook.SWMHook;
import de.exlll.configlib.annotation.ConfigurationElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@ConfigurationElement
@Data @NoArgsConstructor
@Builder @AllArgsConstructor
public class SWMHWorld {

	private static List<String> loadedWorlds = new ArrayList<>();

	@Builder.Default
	private SWMLoaderType loader = SWMLoaderType.FILE;
	@Builder.Default
	private String templateName = "default";
	@Builder.Default
	private String worldName = "default";
	@Builder.Default
	private int amount = 5;

}
