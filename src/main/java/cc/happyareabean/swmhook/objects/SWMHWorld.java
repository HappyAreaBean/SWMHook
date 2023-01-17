package cc.happyareabean.swmhook.objects;

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

	private SWMLoaderType loader = SWMLoaderType.FILE;
	private String templateName = "default";
	private String worldName = "default";
	private int amount = 5;

}
