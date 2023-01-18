package cc.happyareabean.swmhook.utils;

import org.apache.commons.lang3.BooleanUtils;
import org.bukkit.ChatColor;

public class Color {

	public static String translate(String string) {
		return ChatColor.translateAlternateColorCodes('&', string);
	}

	public static String toColorBoolean(boolean value) {
		String str = BooleanUtils.toStringYesNo(value);
		return value ? ChatColor.GREEN + str : ChatColor.RED + str;
	}
}
