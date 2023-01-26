package cc.happyareabean.swmhook.constants;

import cc.happyareabean.swmhook.utils.Color;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import javax.xml.soap.Text;
import java.util.Arrays;
import java.util.List;

import static net.kyori.adventure.text.Component.text;

public class Tags {
	public static final TextComponent SLIME = text(" [SLIME]", NamedTextColor.DARK_GREEN);
	public static final TextComponent SWMH = text(" [SWMH]", NamedTextColor.GREEN);

	public static TextComponent fromProvider(String provider) {
		return text(String.format(" [%s]", provider.toUpperCase()), NamedTextColor.DARK_RED);
	}
}
