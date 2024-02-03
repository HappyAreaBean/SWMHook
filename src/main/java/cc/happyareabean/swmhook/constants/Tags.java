package cc.happyareabean.swmhook.constants;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

import static net.kyori.adventure.text.Component.text;

public class Tags {
	public static final TextComponent SLIME = text(" [SLIME]", NamedTextColor.DARK_GREEN);
	public static final TextComponent SWMH = text(" [SWMH]", NamedTextColor.GREEN);

	public static TextComponent fromProvider(String provider) {
		return text(String.format(" [%s]", provider.toUpperCase()), NamedTextColor.DARK_RED);
	}
}
