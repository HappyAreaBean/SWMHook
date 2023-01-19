package cc.happyareabean.swmhook.constants;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class Constants {
	
	public static final String VERSION = "${pluginVersion}";

	public static final TextComponent RELOAD_WORLD = Component.text("[Do you want to also reload SWMHook worlds?]",
					NamedTextColor.WHITE, TextDecoration.BOLD)
			.hoverEvent(Component.text("Click here to reload worlds"))
			.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/swmhook reloadworlds"));
}
