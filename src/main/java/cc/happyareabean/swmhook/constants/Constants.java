package cc.happyareabean.swmhook.constants;

import cc.happyareabean.swmhook.utils.Color;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.Arrays;
import java.util.List;

public class Constants {
	public static final List<String> LOGO_LIST = Arrays.asList(
			" _____  _    ____  ___ _   _             _    ",
			"/  ___|| |  | |  \\/  || | | |           | |   ",
			"\\ `--. | |  | | .  . || |_| | ___   ___ | | __",
			" `--. \\| |/\\| | |\\/| ||  _  |/ _ \\ / _ \\| |/ /",
			"/\\__/ /\\  /\\  / |  | || | | | (_) | (_) |   < ",
			"\\____/  \\/  \\/\\_|  |_/\\_| |_/\\___/ \\___/|_|\\_\\"
	);
	public static final String PREFIX = Color.translate("&8[&2SWMHook&8] &a");
	public static final String VERSION = "${pluginVersion}";

	public static final TextComponent RELOAD_WORLD = Component.text("[Do you want to also reload SWMHook worlds?]",
					NamedTextColor.WHITE, TextDecoration.BOLD)
			.hoverEvent(Component.text("Click here to reload worlds"))
			.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/swmhook reloadworlds"));

	public static final String HELP_COMMAND_FORMAT = "/swmhook %s";
	public static final String PAGE_TEXT = "Page %s";
	public static final String SWM = "SlimeWorldManager";
}
